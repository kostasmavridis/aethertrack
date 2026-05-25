package com.aethertrack.scheduling.domain;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * A single scheduled dose of one supplement – the PLANNING ENTITY.
 * Timefold assigns each dose a {@code TimeSlot} from the available values.
 */
@PlanningEntity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SupplementDose {

    private Long id;
    private Long regimenItemId;
    private String supplementCode;
    private String supplementCategory;
    private BigDecimal doseQty;
    private String doseUnit;

    /** If true, must be in a slot where nightTime == true (Hard constraint). */
    private boolean nightTimeRequired;

    /** If true, must be in a slot where withMeal == true (Hard constraint). */
    private boolean mealRequired;

    @PlanningVariable(valueRangeProviderRefs = "timeSlotRange")
    private TimeSlot assignedSlot;

    @Override
    public String toString() {
        return supplementCode + "(id=" + id + ", slot=" + assignedSlot + ")";
    }
}
