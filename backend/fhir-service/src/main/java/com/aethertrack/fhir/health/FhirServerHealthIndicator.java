package com.aethertrack.fhir.health;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.aethertrack.fhir.config.FhirProperties;
import org.hl7.fhir.r5.model.CapabilityStatement;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Custom Spring Boot Actuator {@link HealthIndicator} for the HAPI FHIR server.
 *
 * Fetches the {@code CapabilityStatement} (metadata endpoint) and surfaces:
 * <ul>
 *   <li>{@code fhirVersion}  – e.g. "5.0.0"</li>
 *   <li>{@code publisher}   – server software name</li>
 *   <li>{@code baseUrl}     – configured FHIR base URL</li>
 * </ul>
 *
 * Marks status DOWN on any exception so Kubernetes readiness probes
 * ({@code /actuator/health/readiness}) correctly block traffic when
 * the HAPI container is unavailable.
 */
@Component("fhirServer")
public class FhirServerHealthIndicator implements HealthIndicator {

    private final IGenericClient fhirClient;
    private final FhirProperties props;

    public FhirServerHealthIndicator(IGenericClient fhirClient, FhirProperties props) {
        this.fhirClient = fhirClient;
        this.props      = props;
    }

    @Override
    public Health health() {
        try {
            CapabilityStatement cs = fhirClient
                .capabilities()
                .ofType(CapabilityStatement.class)
                .execute();

            String fhirVersion = cs.getFhirVersion() != null
                ? cs.getFhirVersion().toCode() : "unknown";
            String publisher   = cs.getSoftware() != null
                ? cs.getSoftware().getName() : "unknown";

            return Health.up()
                .withDetail("fhirVersion", fhirVersion)
                .withDetail("publisher",   publisher)
                .withDetail("baseUrl",     props.baseUrl())
                .build();

        } catch (Exception ex) {
            return Health.down()
                .withDetail("baseUrl",  props.baseUrl())
                .withDetail("error",    ex.getMessage())
                .build();
        }
    }
}
