package com.aethertrack.scheduling;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(
        partitions = 1,
        topics     = {"aethertrack.regimen.created"}
)
class SchedulingServiceApplicationTests {

    @Test
    void contextLoads() {
        // Spring context boots cleanly with embedded Kafka
    }
}
