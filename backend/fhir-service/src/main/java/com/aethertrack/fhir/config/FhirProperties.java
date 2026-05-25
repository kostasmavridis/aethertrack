package com.aethertrack.fhir.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Externalised FHIR server configuration.
 *
 * Bound from application.yml:
 * <pre>
 * aethertrack:
 *   fhir:
 *     base-url: http://hapi-fhir:8080/fhir
 *     socket-timeout-ms: 10000
 *     connect-timeout-ms: 5000
 * </pre>
 */
@ConfigurationProperties(prefix = "aethertrack.fhir")
public record FhirProperties(
        String  baseUrl,
        int     socketTimeoutMs,
        int     connectTimeoutMs
) {
    /** Sensible defaults so the service starts without any config. */
    public FhirProperties {
        if (baseUrl        == null) baseUrl        = "http://localhost:8080/fhir";
        if (socketTimeoutMs  == 0)  socketTimeoutMs  = 10_000;
        if (connectTimeoutMs == 0)  connectTimeoutMs =  5_000;
    }
}
