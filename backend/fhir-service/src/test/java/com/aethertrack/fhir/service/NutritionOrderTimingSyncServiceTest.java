package com.aethertrack.fhir.service;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.aethertrack.fhir.domain.FhirSyncOutboxEvent;
import com.aethertrack.fhir.domain.RegimenFhirMapping;
import com.aethertrack.fhir.events.OptimizationCompletedPayload;
import com.aethertrack.fhir.repository.FhirSyncOutboxRepository;
import com.aethertrack.fhir.repository.RegimenFhirMappingRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hl7.fhir.r5.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NutritionOrderTimingSyncServiceTest {

    @Mock IGenericClient fhirClient;
    @Mock RegimenFhirMappingRepository mappingRepository;
    @Mock FhirSyncOutboxRepository outboxRepository;

    @Mock ca.uhn.fhir.rest.gclient.IRead                            readStep;
    @Mock ca.uhn.fhir.rest.gclient.IReadExecutable<NutritionOrder>  readExecutable;
    @Mock ca.uhn.fhir.rest.gclient.IUpdate                          updateStep;
    @Mock ca.uhn.fhir.rest.gclient.IUpdateTyped                     updateTyped;

    NutritionOrderTimingSyncService service;

    @BeforeEach
    void setUp() {
        service = new NutritionOrderTimingSyncService(
            fhirClient, mappingRepository, outboxRepository, new ObjectMapper());
    }

    @Test
    void syncTiming_updatesNutritionOrder_andWritesCompletedOutbox() {
        var mapping = RegimenFhirMapping.of(42L, "99", "http://hapi/fhir/NutritionOrder/99");
        when(mappingRepository.findByRegimenId(42L)).thenReturn(Optional.of(mapping));

        // Build a NutritionOrder with one supplement that already has a schedule
        NutritionOrder order = new NutritionOrder();
        var supp = new NutritionOrder.NutritionOrderSupplementComponent();
        var sched = new NutritionOrder.SupplementScheduleComponent();
        sched.addTiming(new Timing());   // pre-populate so hasSchedule() == true
        supp.setSchedule(sched);
        order.addSupplement(supp);

        // Mock read() chain: IRead → IReadTyped (mock as IReadExecutable to avoid type mismatch)
        @SuppressWarnings("unchecked")
        ca.uhn.fhir.rest.gclient.IReadTyped<NutritionOrder> readTyped =
            (ca.uhn.fhir.rest.gclient.IReadTyped<NutritionOrder>) readExecutable;
        when(fhirClient.read()).thenReturn(readStep);
        when(readStep.resource(NutritionOrder.class)).thenReturn(readTyped);
        when(readTyped.withId("99")).thenReturn(readExecutable);
        when(readExecutable.execute()).thenReturn(order);

        when(fhirClient.update()).thenReturn(updateStep);
        when(updateStep.resource(any(NutritionOrder.class))).thenReturn(updateTyped);
        MethodOutcome outcome = new MethodOutcome();
        outcome.setId(new IdType("NutritionOrder", "99", "2"));
        when(updateTyped.execute()).thenReturn(outcome);

        var payload = new OptimizationCompletedPayload(
            42L, "0hard/0soft",
            Instant.parse("2026-05-25T01:00:00Z"),
            List.of(new OptimizationCompletedPayload.DoseAssignment(
                1L, 0, "MORNING", "08:00", "08:30", BigDecimal.ONE, "tablet")));

        service.syncTiming(payload, "corr-1");

        verify(mappingRepository).save(mapping);
        ArgumentCaptor<FhirSyncOutboxEvent> captor = ArgumentCaptor.forClass(FhirSyncOutboxEvent.class);
        verify(outboxRepository).save(captor.capture());
        assertThat(captor.getValue().getEventType()).isEqualTo("fhir.sync.completed");

        // Verify timing was written correctly via R5 API
        Timing t = order.getSupplementFirstRep().getSchedule().getTimingFirstRep();
        assertThat(t.getRepeat().getWhen())
            .anyMatch(w -> w.getValue() == Timing.EventTiming.MORN);
    }

    @Test
    void enqueueFailed_writesFailedOutboxEvent() {
        service.enqueueFailed(42L, "99", "missing mapping", "corr-2");
        ArgumentCaptor<FhirSyncOutboxEvent> captor = ArgumentCaptor.forClass(FhirSyncOutboxEvent.class);
        verify(outboxRepository).save(captor.capture());
        assertThat(captor.getValue().getEventType()).isEqualTo("fhir.sync.failed");
        assertThat(captor.getValue().getPayload()).contains("missing mapping");
    }
}
