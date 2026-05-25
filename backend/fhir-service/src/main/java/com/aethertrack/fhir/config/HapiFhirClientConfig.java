package com.aethertrack.fhir.config;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.IRestfulClientFactory;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures a singleton HAPI FHIR R5 {@link IGenericClient}.
 *
 * The client is thread-safe and should be injected wherever FHIR calls
 * are needed (health indicator, NutritionOrder writer in Slice 11, etc.).
 *
 * Server capability validation is disabled at startup so the service boots
 * even when the HAPI container is temporarily unavailable; the custom
 * {@link com.aethertrack.fhir.health.FhirServerHealthIndicator} surfaces
 * connectivity state instead.
 */
@Configuration
public class HapiFhirClientConfig {

    private final FhirProperties props;

    public HapiFhirClientConfig(FhirProperties props) {
        this.props = props;
    }

    @Bean
    public FhirContext fhirContext() {
        return FhirContext.forR5();
    }

    @Bean
    public IGenericClient fhirClient(FhirContext fhirContext) {
        IRestfulClientFactory factory = fhirContext.getRestfulClientFactory();
        factory.setSocketTimeout(props.socketTimeoutMs());
        factory.setConnectTimeout(props.connectTimeoutMs());
        factory.setServerValidationMode(ServerValidationModeEnum.NEVER);

        return fhirContext.newRestfulGenericClient(props.baseUrl());
    }
}
