package com.aethertrack.intake.service;

import com.aethertrack.intake.domain.IntakeLog;
import com.aethertrack.intake.domain.OutboxEvent;
import com.aethertrack.intake.events.DomainEvent;
import com.aethertrack.intake.events.IntakeLoggedPayload;
import com.aethertrack.intake.events.KafkaTopics;
import com.aethertrack.intake.repository.IntakeLogRepository;
import com.aethertrack.intake.repository.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class IntakeLogService {

    private final IntakeLogRepository intakeLogRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public IntakeLog create(IntakeLog intakeLog, String correlationId) {
        IntakeLog saved = intakeLogRepository.save(intakeLog);
        outboxEventRepository.save(OutboxEvent.pending(
                KafkaTopics.INTAKE_LOGGED,
                saved.getId(),
                correlationId,
                toEventJson(saved, correlationId)
        ));
        log.info("[IntakeLogService] saved intakeLogId={} regimenItemId={}", saved.getId(), saved.getRegimenItemId());
        return saved;
    }

    private String toEventJson(IntakeLog saved, String correlationId) {
        try {
            var event = new DomainEvent<>(
                    UUID.randomUUID().toString(),
                    "IntakeLogged",
                    "1",
                    Instant.now(),
                    correlationId,
                    null,
                    saved.getPatientId(),
                    new IntakeLoggedPayload(
                            saved.getId(),
                            saved.getPatientId(),
                            saved.getRegimenItemId(),
                            saved.getTakenDateTime(),
                            saved.getQuantity()
                    )
            );
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize IntakeLogged event", e);
        }
    }
}
