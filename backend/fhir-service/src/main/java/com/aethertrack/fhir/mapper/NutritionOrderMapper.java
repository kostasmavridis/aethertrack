package com.aethertrack.fhir.mapper;

import com.aethertrack.fhir.events.RegimenCreatedPayload;
import com.aethertrack.fhir.events.RegimenCreatedPayload.RegimenItem;
import org.hl7.fhir.r5.model.*;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Maps a {@link RegimenCreatedPayload} to an R5 {@link NutritionOrder}.
 *
 * Mapping strategy (minimal viable FHIR resource):
 * <ul>
 *   <li>subject           → Patient/{patientId}</li>
 *   <li>orderer           → Practitioner/aethertrack-system (placeholder)</li>
 *   <li>status            → ACTIVE</li>
 *   <li>intent            → ORDER</li>
 *   <li>dateTime          → now()</li>
 *   <li>Per RegimenItem   → NutritionOrder.supplement with Quantity + Timing</li>
 *   <li>scheduleWindow    → Timing.when (MORN / MORN+EVE / PCM / CV / HS / PCV)</li>
 * </ul>
 *
 * Slice 12 will UPDATE the Timing fields when OptimizationCompleted arrives.
 */
@Component
public class NutritionOrderMapper {

    public NutritionOrder toNutritionOrder(RegimenCreatedPayload payload) {
        NutritionOrder no = new NutritionOrder();

        // ── Core fields ──────────────────────────────────────────────────────
        no.setSubject(new Reference("Patient/" + payload.patientId()));
        no.setOrderer(new Reference("Practitioner/aethertrack-system"));
        no.setStatus(NutritionOrder.NutritiionOrderStatus.ACTIVE);
        no.setIntent(NutritionOrder.NutritiionOrderIntent.ORDER);
        no.setDateTime(new DateTimeType(new Date()));

        // ── Note: regimenId stored as NutritionOrder.note for traceability ──
        no.addNote(new Annotation()
            .setText("aethertrack:regimenId=" + payload.regimenId()));

        // ── One supplement entry per regimen item ─────────────────────────
        for (RegimenItem item : payload.items()) {
            NutritionOrder.NutritionOrderSupplementComponent supplement =
                new NutritionOrder.NutritionOrderSupplementComponent();

            // Type: supplement code as CodeableConcept text
            supplement.setType(new CodeableConcept().setText(item.supplementCode()));

            // Quantity: dose amount + unit
            if (item.doseQty() != null && item.doseUnit() != null) {
                supplement.setQuantity(new Quantity()
                    .setValue(item.doseQty())
                    .setUnit(item.doseUnit()));
            }

            // Schedule: derive Timing from frequencyPerDay + scheduleWindow
            supplement.addSchedule(buildSchedule(item));

            no.addSupplement(supplement);
        }

        return no;
    }

    // ── Timing derivation ────────────────────────────────────────────────────

    private NutritionOrder.NutritionOrderSupplementScheduleComponent
            buildSchedule(RegimenItem item) {

        var schedule = new NutritionOrder.NutritionOrderSupplementScheduleComponent();
        Timing timing = new Timing();
        Timing.TimingRepeatComponent repeat = new Timing.TimingRepeatComponent();

        // Frequency: how many times per day
        int freq = item.frequencyPerDay() != null ? item.frequencyPerDay() : 1;
        repeat.setFrequency(freq);
        repeat.setPeriod(1);
        repeat.setPeriodUnit(Timing.UnitsOfTime.D);

        // When: map scheduleWindow hint to FHIR Timing.when codes
        mapScheduleWindow(item.scheduleWindow(), repeat);

        timing.setRepeat(repeat);
        schedule.setTiming(timing);
        return schedule;
    }

    /**
     * Maps AetherTrack scheduleWindow strings to FHIR {@link Timing.EventTiming} codes.
     *
     * | scheduleWindow | FHIR when        |
     * |----------------|------------------|
     * | MORNING        | MORN             |
     * | MIDDAY         | NOON             |
     * | EVENING        | EVE              |
     * | NIGHT          | HS  (hour of sleep) |
     * | WITH_MEAL/MEAL | PCM (after meal) |
     * | null / other   | (not set)        |
     */
    private void mapScheduleWindow(String scheduleWindow,
                                   Timing.TimingRepeatComponent repeat) {
        if (scheduleWindow == null) return;
        switch (scheduleWindow.toUpperCase()) {
            case "MORNING"   -> repeat.addWhen(Timing.EventTiming.MORN);
            case "MIDDAY"    -> repeat.addWhen(Timing.EventTiming.NOON);
            case "EVENING"   -> repeat.addWhen(Timing.EventTiming.EVE);
            case "NIGHT"     -> repeat.addWhen(Timing.EventTiming.HS);
            case "WITH_MEAL",
                 "MEAL"      -> repeat.addWhen(Timing.EventTiming.PCM);
            default          -> { /* leave unset; Slice 12 will fill from solver */ }
        }
    }
}
