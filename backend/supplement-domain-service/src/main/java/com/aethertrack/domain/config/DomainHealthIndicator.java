package com.aethertrack.domain.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Custom health indicator exposed at /actuator/health.
 * Extended in later slices to probe Kafka and Postgres.
 */
@Component("domainService")
public class DomainHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        return Health.up()
                .withDetail("service", "supplement-domain-service")
                .withDetail("slice",   "1")
                .build();
    }
}
