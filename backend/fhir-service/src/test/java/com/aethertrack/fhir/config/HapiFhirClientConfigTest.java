package com.aethertrack.fhir.config;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that the Spring context loads and the HAPI FHIR beans are present.
 * No real HAPI server needed – ServerValidationModeEnum.NEVER prevents
 * the client from hitting the server on startup.
 */
@SpringBootTest
@TestPropertySource(properties = {
    "aethertrack.fhir.base-url=http://localhost:18080/fhir",
    "spring.kafka.bootstrap-servers=localhost:19092"
})
class HapiFhirClientConfigTest {

    @Autowired FhirContext   fhirContext;
    @Autowired IGenericClient fhirClient;
    @Autowired FhirProperties props;

    @Test void contextLoads() {
        assertThat(fhirContext).isNotNull();
        assertThat(fhirClient).isNotNull();
    }

    @Test void baseUrl_matchesConfiguration() {
        assertThat(props.baseUrl()).isEqualTo("http://localhost:18080/fhir");
    }

    @Test void fhirContext_isR5() {
        assertThat(fhirContext.getVersion().getVersion().name()).isEqualTo("R5");
    }
}
