package com.aethertrack.fhir.mapper;

import com.aethertrack.fhir.events.RegimenCreatedPayload;
import com.aethertrack.fhir.events.RegimenCreatedPayload.RegimenItem;
import org.hl7.fhir.r5.model.NutritionOrder;
import org.hl7.fhir.r5.model.Timing;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NutritionOrderMapperTest {

    private final NutritionOrderMapper mapper = new NutritionOrderMapper();

    private RegimenCreatedPayload payload(List<RegimenItem> items) {
        return new RegimenCreatedPayload(42L, "p-001", "Test Regimen", items);
    }

    // R5 helper: supplement.getSchedule() returns a singular SupplementScheduleComponent
    private Timing timing(NutritionOrder.NutritionOrderSupplementComponent supplement) {
        return supplement.getSchedule().getTimingFirstRep();
    }

    @Test void mapsSubjectReference() {
        var no = mapper.toNutritionOrder(payload(List.of(
            new RegimenItem(1L, 10L, "VIT-D3", BigDecimal.ONE, "tablet", 1, null))));
        assertThat(no.getSubject().getReference()).isEqualTo("Patient/p-001");
    }

    @Test void mapsAllItemsToSupplements() {
        var no = mapper.toNutritionOrder(payload(List.of(
            new RegimenItem(1L, 10L, "VIT-D3",  BigDecimal.ONE, "IU",  1, null),
            new RegimenItem(2L, 11L, "MAG-GLY", BigDecimal.TEN, "mg",  1, "NIGHT"))));
        assertThat(no.getSupplement()).hasSize(2);
    }

    @Test void mapsScheduleWindow_MORNING_to_MORN() {
        var no = mapper.toNutritionOrder(payload(List.of(
            new RegimenItem(1L, 10L, "VIT-C", BigDecimal.ONE, "tablet", 1, "MORNING"))));
        Timing t = timing(no.getSupplement().get(0));
        assertThat(t.getRepeat().getWhen())
            .anyMatch(w -> w.getValue() == Timing.EventTiming.MORN);
    }

    @Test void mapsScheduleWindow_NIGHT_to_HS() {
        var no = mapper.toNutritionOrder(payload(List.of(
            new RegimenItem(1L, 10L, "MAG-GLY", BigDecimal.ONE, "tablet", 1, "NIGHT"))));
        Timing t = timing(no.getSupplement().get(0));
        assertThat(t.getRepeat().getWhen())
            .anyMatch(w -> w.getValue() == Timing.EventTiming.HS);
    }

    @Test void mapsFrequencyPerDay() {
        var no = mapper.toNutritionOrder(payload(List.of(
            new RegimenItem(1L, 10L, "VIT-D3", BigDecimal.ONE, "IU", 3, null))));
        int freq = timing(no.getSupplement().get(0)).getRepeat().getFrequency();
        assertThat(freq).isEqualTo(3);
    }
}
