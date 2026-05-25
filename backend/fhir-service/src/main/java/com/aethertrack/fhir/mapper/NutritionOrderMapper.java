package com.aethertrack.fhir.mapper;

import com.aethertrack.fhir.events.RegimenCreatedPayload;
import com.aethertrack.fhir.events.RegimenCreatedPayload.RegimenItem;
import org.hl7.fhir.r5.model.*;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Maps a {@link RegimenCreatedPayload} to an R5 {@link NutritionOrder}.
 *
 * Confirmed R5 HAPI FHIR API:
 *  - NutritionOrderSupplementComponent.setSchedule(SupplementScheduleComponent)  [singular]
 *  - SupplementScheduleComponent.addTiming(Timing)
 *  - SupplementScheduleComponent.getTimingFirstRep()
 */
@Component
public class NutritionOrderMapper {

    public NutritionOrder toNutritionOrder(RegimenCreatedPayload payload) {
        NutritionOrder no = new NutritionOrder();

        no.setSubject(new Reference("Patient/" + payload.patientId()));
        no.setOrderer(new Reference("Practitioner/aethertrack-system"));
        no.setStatus(Enumerations.RequestStatus.ACTIVE);
        no.setIntent(Enumerations.RequestIntent.ORDER);
        no.setDateTime(new Date());

        no.addNote(new Annotation()
            .setText("aethertrack:regimenId=" + payload.regimenId()));

        for (RegimenItem item : payload.items()) {
            NutritionOrder.NutritionOrderSupplementComponent supplement =
                new NutritionOrder.NutritionOrderSupplementComponent();

            supplement.setType(new CodeableReference()
                .setConcept(new CodeableConcept().setText(item.supplementCode())));

            if (item.doseQty() != null && item.doseUnit() != null) {
                supplement.setQuantity(new Quantity()
                    .setValue(item.doseQty())
                    .setUnit(item.doseUnit()));
            }

            // R5: schedule is singular; timing list lives on SupplementScheduleComponent
            NutritionOrder.SupplementScheduleComponent schedule =
                new NutritionOrder.SupplementScheduleComponent();
            schedule.addTiming(buildTiming(item));
            supplement.setSchedule(schedule);

            no.addSupplement(supplement);
        }

        return no;
    }

    private Timing buildTiming(RegimenItem item) {
        Timing timing = new Timing();
        Timing.TimingRepeatComponent repeat = new Timing.TimingRepeatComponent();

        int freq = item.frequencyPerDay() != null ? item.frequencyPerDay() : 1;
        repeat.setFrequency(freq);
        repeat.setPeriod(1);
        repeat.setPeriodUnit(Timing.UnitsOfTime.D);

        mapScheduleWindow(item.scheduleWindow(), repeat);

        timing.setRepeat(repeat);
        return timing;
    }

    private void mapScheduleWindow(String scheduleWindow,
                                   Timing.TimingRepeatComponent repeat) {
        if (scheduleWindow == null) return;
        switch (scheduleWindow.toUpperCase()) {
            case "MORNING"            -> repeat.addWhen(Timing.EventTiming.MORN);
            case "MIDDAY"             -> repeat.addWhen(Timing.EventTiming.NOON);
            case "EVENING"            -> repeat.addWhen(Timing.EventTiming.EVE);
            case "NIGHT"              -> repeat.addWhen(Timing.EventTiming.HS);
            case "WITH_MEAL", "MEAL" -> repeat.addWhen(Timing.EventTiming.PCM);
            default                   -> { /* leave unset */ }
        }
    }
}
