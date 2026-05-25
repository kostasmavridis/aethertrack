package com.aethertrack.fhir.health;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.IFetchConformanceTyped;
import ca.uhn.fhir.rest.gclient.IFetchConformanceUntyped;
import com.aethertrack.fhir.config.FhirProperties;
import org.hl7.fhir.r5.model.CapabilityStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FhirServerHealthIndicatorTest {

    @Mock IGenericClient      fhirClient;
    @Mock IFetchConformanceUntyped  conformanceUntyped;
    @Mock IFetchConformanceTyped<CapabilityStatement> conformanceTyped;

    FhirServerHealthIndicator indicator;

    @BeforeEach void setup() {
        FhirProperties props = new FhirProperties(
            "http://localhost:8080/fhir", 10_000, 5_000);
        indicator = new FhirServerHealthIndicator(fhirClient, props);
    }

    @Test void health_isUp_whenCapabilityStatementFetched() {
        CapabilityStatement cs = new CapabilityStatement();
        cs.setFhirVersion(org.hl7.fhir.r5.model.Enumerations.FHIRVersion._5_0_0);
        CapabilityStatement.CapabilityStatementSoftwareComponent sw =
            new CapabilityStatement.CapabilityStatementSoftwareComponent();
        sw.setName("HAPI FHIR Server");
        cs.setSoftware(sw);

        when(fhirClient.capabilities()).thenReturn(conformanceUntyped);
        when(conformanceUntyped.ofType(CapabilityStatement.class)).thenReturn(conformanceTyped);
        when(conformanceTyped.execute()).thenReturn(cs);

        Health h = indicator.health();
        assertThat(h.getStatus()).isEqualTo(Status.UP);
        assertThat(h.getDetails()).containsKey("fhirVersion");
        assertThat(h.getDetails().get("publisher")).isEqualTo("HAPI FHIR Server");
    }

    @Test void health_isDown_onException() {
        when(fhirClient.capabilities()).thenThrow(new RuntimeException("Connection refused"));

        Health h = indicator.health();
        assertThat(h.getStatus()).isEqualTo(Status.DOWN);
        assertThat(h.getDetails()).containsKey("error");
    }
}
