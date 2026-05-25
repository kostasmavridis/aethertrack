package com.aethertrack.intake.service;

import com.aethertrack.intake.domain.IntakeLog;
import com.aethertrack.intake.domain.OutboxEvent;
import com.aethertrack.intake.repository.IntakeLogRepository;
import com.aethertrack.intake.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IntakeLogServiceTest {

    @Mock IntakeLogRepository intakeLogRepository;
    @Mock OutboxEventRepository outboxEventRepository;

    IntakeLogService service;

    @BeforeEach
    void setUp() {
        service = new IntakeLogService(intakeLogRepository, outboxEventRepository, new ObjectMapper());
    }

    @Test
    void create_savesIntakeLog_andOutboxEvent() {
        IntakeLog saved = IntakeLog.of("patient-1", 101L, Instant.parse("2026-05-25T04:00:00Z"), new BigDecimal("1.000"));
        saved.setId(1L);
        when(intakeLogRepository.save(any(IntakeLog.class))).thenReturn(saved);

        IntakeLog result = service.create(IntakeLog.of("patient-1", 101L, Instant.parse("2026-05-25T04:00:00Z"), new BigDecimal("1.000")), "corr-1");

        assertThat(result.getId()).isEqualTo(1L);
        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository).save(captor.capture());
        assertThat(captor.getValue().getEventType()).isEqualTo("intake.logged");
        assertThat(captor.getValue().getPayload()).contains("IntakeLogged");
    }
}
