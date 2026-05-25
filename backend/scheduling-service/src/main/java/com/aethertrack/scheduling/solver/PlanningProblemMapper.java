package com.aethertrack.scheduling.solver;

import com.aethertrack.scheduling.domain.SupplementDose;
import com.aethertrack.scheduling.domain.SupplementSchedule;
import com.aethertrack.scheduling.domain.TimeSlot;
import com.aethertrack.scheduling.events.RegimenCreatedPayload;
import com.aethertrack.scheduling.events.RegimenCreatedPayload.RegimenItemPayload;
import com.aethertrack.scheduling.repository.RegimenItemReadModel;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Maps regimen data (from DB read model OR event payload) into a
 * {@link SupplementSchedule} planning problem ready for the Timefold solver.
 *
 * Each RegimenItem with frequencyPerDay=N is expanded into N SupplementDose entities.
 */
@Component
public class PlanningProblemMapper {

    public SupplementSchedule fromReadModels(Long regimenId, String patientId,
                                              List<RegimenItemReadModel> items) {
        List<SupplementDose> doses = new ArrayList<>();
        AtomicLong seq = new AtomicLong(1);

        for (RegimenItemReadModel item : items) {
            int freq = item.frequencyPerDay() != null ? item.frequencyPerDay() : 1;
            for (int i = 0; i < freq; i++) {
                SupplementDose dose = new SupplementDose();
                dose.setId(seq.getAndIncrement());
                dose.setRegimenItemId(item.itemId());
                dose.setSupplementCode(item.supplementCode());
                dose.setSupplementCategory(item.supplementCategory());
                dose.setDoseQty(item.doseQty());
                dose.setDoseUnit(item.doseUnit());
                dose.setNightTimeRequired(item.nightTimeRequired());
                dose.setMealRequired(item.mealRequired());
                doses.add(dose);
            }
        }

        return SupplementSchedule.of(regimenId, patientId, TimeSlot.defaultSlots(), doses);
    }

    /**
     * Fallback: build from event payload when DB is unavailable or empty.
     * Derives nightTimeRequired/mealRequired from scheduleWindow hint.
     */
    public SupplementSchedule fromEventPayload(RegimenCreatedPayload payload) {
        List<SupplementDose> doses = new ArrayList<>();
        AtomicLong seq = new AtomicLong(1);

        for (RegimenItemPayload item : payload.items()) {
            int freq = item.frequencyPerDay() != null ? item.frequencyPerDay() : 1;
            boolean nightRequired = "NIGHT".equalsIgnoreCase(item.scheduleWindow());
            boolean mealRequired  = "MEAL".equalsIgnoreCase(item.scheduleWindow())
                                 || "WITH_MEAL".equalsIgnoreCase(item.scheduleWindow());
            for (int i = 0; i < freq; i++) {
                SupplementDose dose = new SupplementDose();
                dose.setId(seq.getAndIncrement());
                dose.setRegimenItemId(item.itemId());
                dose.setSupplementCode(item.supplementCode());
                dose.setSupplementCategory("UNKNOWN");
                dose.setDoseQty(item.doseQty());
                dose.setDoseUnit(item.doseUnit());
                dose.setNightTimeRequired(nightRequired);
                dose.setMealRequired(mealRequired);
                doses.add(dose);
            }
        }

        return SupplementSchedule.of(payload.regimenId(), payload.patientId(),
                                     TimeSlot.defaultSlots(), doses);
    }
}
