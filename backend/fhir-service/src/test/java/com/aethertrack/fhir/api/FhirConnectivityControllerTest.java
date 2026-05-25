package com.aethertrack.fhir.api;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.IFetchConformanceTyped;
import ca.uhn.fhir.rest.gclient.IFetchConformanceUntyped;
import com.aethertrack.fhir.config.FhirProperties;
import org.hl7.fhir.r5.model.CapabilityStatement;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FhirConnectivityController.class)
class FhirConnectivityControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean IGenericClient fhirClient;
    @MockitoBean FhirProperties props;

    @SuppressWarnings("unchecked")
    @Test void connectivity_returns200_withFhirVersion() throws Exception {
        CapabilityStatement cs = new CapabilityStatement();
        cs.setFhirVersion(org.hl7.fhir.r5.model.Enumerations.FHIRVersion._5_0_0);
        CapabilityStatement.CapabilityStatementSoftwareComponent sw =
            new CapabilityStatement.CapabilityStatementSoftwareComponent();
        sw.setName("HAPI FHIR");
        cs.setSoftware(sw);

        IFetchConformanceUntyped untyped = mock(IFetchConformanceUntyped.class);
        IFetchConformanceTyped<CapabilityStatement> typed = mock(IFetchConformanceTyped.class);
        when(fhirClient.capabilities()).thenReturn(untyped);
        when(untyped.ofType(CapabilityStatement.class)).thenReturn(typed);
        when(typed.execute()).thenReturn(cs);
        when(props.baseUrl()).thenReturn("http://hapi:8080/fhir");

        mockMvc.perform(get("/api/fhir/connectivity"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"))
            .andExpect(jsonPath("$.fhirVersion").value("5.0.0"))
            .andExpect(jsonPath("$.publisher").value("HAPI FHIR"))
            .andExpect(jsonPath("$.baseUrl").value("http://hapi:8080/fhir"))
            .andExpect(jsonPath("$.checkedAt").isString());
    }

    @Test void connectivity_returns503_whenFhirDown() throws Exception {
        when(fhirClient.capabilities()).thenThrow(new RuntimeException("HAPI unreachable"));
        when(props.baseUrl()).thenReturn("http://hapi:8080/fhir");

        mockMvc.perform(get("/api/fhir/connectivity"))
            .andExpect(status().isServiceUnavailable())
            .andExpect(jsonPath("$.status").value("DOWN"))
            .andExpect(jsonPath("$.error").value(containsString("HAPI unreachable")));
    }
}
