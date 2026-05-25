package com.aethertrack.fhir.service;

import com.aethertrack.fhir.domain.FhirSyncOutboxEvent;
import com.aethertrack.fhir.repository.FhirSyncOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class FhirSyncOutboxRelayService {

    private final FhirSyncOutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelayString = "${aethertrack.fhir.sync-outbox.poll-ms:2000}")
    @Transactional
    public void relayPending() {
        for (FhirSyncOutboxEvent event : outboxRepository.findTop20ByStatusOrderByCreatedAtAsc("PENDING")) {
            try {
                kafkaTemplate.send(event.getEventType(), String.valueOf(event.getAggregateId()), event.getPayload()).get();
                event.setStatus("SENT");
                event.setAttemptCount(event.getAttemptCount() + 1);
                event.setUpdatedAt(Instant.now());
                outboxRepository.save(event);
                log.info("[FhirSyncOutboxRelayService] SENT id={} topic={} aggregateId={}",
                        event.getId(), event.getEventType(), event.getAggregateId());
            } catch (Exception ex) {
                event.setAttemptCount(event.getAttemptCount() + 1);
                event.setLastError(ex.getMessage());
                event.setUpdatedAt(Instant.now());
                if (event.getAttemptCount() >= 5) {
                    event.setStatus("FAILED");
                }
                outboxRepository.save(event);
                log.error("[FhirSyncOutboxRelayService] FAILED id={} topic={} attempts={}: {}",
                        event.getId(), event.getEventType(), event.getAttemptCount(), ex.getMessage(), ex);
            }
        }
    }
}
