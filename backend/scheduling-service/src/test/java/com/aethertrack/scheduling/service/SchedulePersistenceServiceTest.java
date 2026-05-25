package com.aethertrack.scheduling.service;

import com.aethertrack.scheduling.domain.ScheduledDose;
import com.aethertrack.scheduling.domain.SupplementDose;
import com.aethertrack.scheduling.domain.SupplementSchedule;
import com.aethertrack.scheduling.domain.TimeSlot;
import com.aethertrack.scheduling.outbox.OutboxEvent;
import com.aethertrack.scheduling.outbox.OutboxEventRepository;
import com.aethertrack.scheduling.outbox.OutboxStatus;
import com.aethertrack.scheduling.repository.ScheduledDoseRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchedulePersistenceServiceTest {

    @Mock ScheduledDoseRepository scheduledDoseRepository;
    @Mock OutboxEventRepository   outboxEventRepository;
    SchedulePersistenceService service;

    @BeforeEach void setup() {
        ObjectMapper om = new ObjectMapper().registerModule(new JavaTimeModule());
        service = new SchedulePersistenceService(scheduledDoseRepository, outboxEventRepository, om);
    }

    private SupplementSchedule buildSolution() {
        TimeSlot morning = TimeSlot.of("MORNING", LocalTime.of(7,0),  LocalTime.of(9,0),  true,  false);
        TimeSlot night   = TimeSlot.of("NIGHT",   LocalTime.of(21,0), LocalTime.of(23,0), false, true);

        SupplementDose d1 = new SupplementDose();
        d1.setId(1L); d1.setRegimenItemId(10L); d1.setSupplementCode("VIT-D3");
        d1.setDoseQty(BigDecimal.ONE); d1.setDoseUnit("tablet"); d1.setAssignedSlot(morning);

        SupplementDose d2 = new SupplementDose();
        d2.setId(2L); d2.setRegimenItemId(11L); d2.setSupplementCode("MAG-GLY");
        d2.setDoseQty(BigDecimal.ONE); d2.setDoseUnit("tablet"); d2.setNightTimeRequired(true);
        d2.setAssignedSlot(night);

        SupplementSchedule sol = SupplementSchedule.of(42L, "p1", List.of(morning, night), List.of(d1, d2));
        sol.setScore(HardSoftScore.of(0, -3));
        return sol;
    }

    @Test void persistAndEnqueue_savesScheduledDoses() {
        service.persistAndEnqueue(buildSolution(), "corr-1");
        verify(scheduledDoseRepository).deleteByRegimenId(42L);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ScheduledDose>> captor = ArgumentCaptor.forClass(List.class);
        verify(scheduledDoseRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(2);
    }

    @Test void persistAndEnqueue_writesOutboxEventAsPending() {
        service.persistAndEnqueue(buildSolution(), "corr-1");
        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository).save(captor.capture());
        OutboxEvent evt = captor.getValue();
        assertThat(evt.getEventType()).isEqualTo("OptimizationCompleted");
        assertThat(evt.getAggregateId()).isEqualTo("42");
        assertThat(evt.getCorrelationId()).isEqualTo("corr-1");
        assertThat(evt.getStatus()).isEqualTo(OutboxStatus.PENDING);
        assertThat(evt.getPayload()).contains("\"regimenId\":42");
        assertThat(evt.getPayload()).contains("VIT-D3");
        assertThat(evt.getPayload()).contains("MAG-GLY");
    }

    @Test void persistAndEnqueue_scores_arePropagated() {
        service.persistAndEnqueue(buildSolution(), null);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ScheduledDose>> captor = ArgumentCaptor.forClass(List.class);
        verify(scheduledDoseRepository).saveAll(captor.capture());
        captor.getValue().forEach(sd -> {
            assertThat(sd.getHardScore()).isEqualTo(0);
            assertThat(sd.getSoftScore()).isEqualTo(-3);
        });
    }
}
