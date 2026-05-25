package com.aethertrack.domain;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Full-context smoke test.
 * Requires: cd infra && docker compose up -d postgres kafka
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
class SupplementDomainServiceApplicationTest {

    @Test
    void contextLoads() {
        // passes if Spring context starts cleanly
    }
}
