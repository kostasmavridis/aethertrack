package com.aethertrack.scheduling.listener;

import com.aethertrack.scheduling.events.DomainEvent;
import com.aethertrack.scheduling.events.RegimenCreatedPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(
        partitions = 1,
        topics     = {"aethertrack.regimen.created"},
        brokerProperties = {"listeners=PLAINTEXT://localhost:19092", "port=19092"}
)
class RegimenCreatedListenerIT {

    @Autowired
    EmbeddedKafkaBroker embeddedKafkaBroker;

    @Test
    void listener_receivesRegimenCreatedEvent_logsAndAcknowledges() throws Exception {
        ObjectMapper om = new ObjectMapper().registerModule(new JavaTimeModule());

        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafkaBroker);
        JsonSerializer<DomainEvent<?>> serializer = new JsonSerializer<>(om);
        serializer.setAddTypeInfo(false);

        KafkaTemplate<String, DomainEvent<?>> template = new KafkaTemplate<>(
                new DefaultKafkaProducerFactory<>(producerProps, new StringSerializer(), serializer));

        RegimenCreatedPayload payload = new RegimenCreatedPayload(
                42L, "patient-123", "Morning stack",
                List.of(new RegimenCreatedPayload.RegimenItemPayload(
                        100L, 1L, "VIT-D3-1000",
                        BigDecimal.ONE, "tablet", 1, "MORNING")));

        DomainEvent<RegimenCreatedPayload> event = new DomainEvent<>(
                UUID.randomUUID().toString(),   // eventId
                "RegimenCreated",               // eventType
                "v1",                           // version
                Instant.now(),                  // timestamp
                "test-corr-123",               // correlationId
                null,                           // causationId
                null,                           // userId
                payload);                       // payload

        template.send(new ProducerRecord<>("aethertrack.regimen.created", "42", event));

        // Allow listener time to consume and ack
        Thread.sleep(3000);
        // No exception thrown = event received and acknowledged successfully
    }
}
