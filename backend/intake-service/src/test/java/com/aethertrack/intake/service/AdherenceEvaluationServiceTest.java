package com.aethertrack.intake.service;

import com.aethertrack.intake.domain.AdherenceSummary;
import com.aethertrack.intake.domain.OutboxEvent;
import com.aethertrack.intake.domain.ScheduledDoseRef;
import com.aethertrack.intake.repository.AdherenceSummaryRepository;
import com.aethertrack.intake.repository.OutboxEventRepository;
import com.aethertrack.intake.repository.ScheduledDoseRefRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdherenceEvaluationServiceTest {

    @Mock ScheduledDoseRefRepository  scheduleRepo;
    @Mock AdherenceSummaryRepository  summaryRepo;
    @Mock OutboxEventRepository       outboxRepo;

    AdherenceEvaluationService service;

    @BeforeEach
    void setUp() {
        service = new AdherenceEvaluationService(scheduleRepo, summaryRepo, outboxRepo, new ObjectMapper());
    }

    private ScheduledDoseRef window(LocalTime start, LocalTime end) {
        return ScheduledDoseRef.of(101L, "patient-1", "MORNING", start, end);
    }

    private Instant atUtc(int hour, int minute) {
        return Instant.parse("2026-05-25T%02d:%02d:00Z".formatted(hour, minute));
    }

    private AdherenceSummary stubbedSave(String outcome, Integer dev) {
        AdherenceSummary s = AdherenceSummary.of("patient-1", 101L, 1L, outcome, dev);
        s.setId(99L);
        return s;
    }

    @Test
    void evaluate_ON_TIME_when_within_window() {
        when(scheduleRepo.findFirstByRegimenItemId(101L))
            .thenReturn(Optional.of(window(LocalTime.of(8, 0), LocalTime.of(9, 0))));
        when(summaryRepo.save(any())).thenAnswer(inv -> { AdherenceSummary s = inv.getArgument(0); s.setId(99L); return s; });

        AdherenceSummary result = service.evaluate("patient-1", 101L, 1L, atUtc(8, 30), "c1");

        assertThat(result.getOutcome()).isEqualTo("ON_TIME");
        assertThat(result.getDeviationMins()).isEqualTo(0);
    }

    @Test
    void evaluate_LATE_when_after_window() {
        when(scheduleRepo.findFirstByRegimenItemId(101L))
            .thenReturn(Optional.of(window(LocalTime.of(8, 0), LocalTime.of(9, 0))));
        when(summaryRepo.save(any())).thenAnswer(inv -> { AdherenceSummary s = inv.getArgument(0); s.setId(99L); return s; });

        AdherenceSummary result = service.evaluate("patient-1", 101L, 1L, atUtc(10, 0), "c1");

        assertThat(result.getOutcome()).isEqualTo("LATE");
        assertThat(result.getDeviationMins()).isPositive();
    }

    @Test
    void evaluate_EARLY_when_before_window() {
        when(scheduleRepo.findFirstByRegimenItemId(101L))
            .thenReturn(Optional.of(window(LocalTime.of(8, 0), LocalTime.of(9, 0))));
        when(summaryRepo.save(any())).thenAnswer(inv -> { AdherenceSummary s = inv.getArgument(0); s.setId(99L); return s; });

        AdherenceSummary result = service.evaluate("patient-1", 101L, 1L, atUtc(7, 0), "c1");

        assertThat(result.getOutcome()).isEqualTo("EARLY");
        assertThat(result.getDeviationMins()).isNegative();
    }

    @Test
    void evaluate_UNSCHEDULED_when_no_ref() {
        when(scheduleRepo.findFirstByRegimenItemId(101L)).thenReturn(Optional.empty());
        when(summaryRepo.save(any())).thenAnswer(inv -> { AdherenceSummary s = inv.getArgument(0); s.setId(99L); return s; });

        AdherenceSummary result = service.evaluate("patient-1", 101L, 1L, atUtc(8, 0), null);

        assertThat(result.getOutcome()).isEqualTo("UNSCHEDULED");
    }

    @Test
    void evaluate_enqueues_adherence_evaluated_outbox_event() {
        when(scheduleRepo.findFirstByRegimenItemId(101L))
            .thenReturn(Optional.of(window(LocalTime.of(8, 0), LocalTime.of(9, 0))));
        when(summaryRepo.save(any())).thenAnswer(inv -> { AdherenceSummary s = inv.getArgument(0); s.setId(99L); return s; });

        service.evaluate("patient-1", 101L, 1L, atUtc(8, 30), "corr-1");

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxRepo).save(captor.capture());
        assertThat(captor.getValue().getEventType()).isEqualTo("adherence.evaluated");
        assertThat(captor.getValue().getPayload()).contains("AdherenceEvaluated");
    }
}
