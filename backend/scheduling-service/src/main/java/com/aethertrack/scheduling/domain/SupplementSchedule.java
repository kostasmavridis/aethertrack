package com.aethertrack.scheduling.domain;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * The PLANNING SOLUTION: wires together time slots (value range),
 * supplement doses (planning entities), and the score.
 */
@PlanningSolution
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SupplementSchedule {

    private Long regimenId;
    private String patientId;

    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "timeSlotRange")
    private List<TimeSlot> timeSlots;

    @PlanningEntityCollectionProperty
    private List<SupplementDose> doses;

    @PlanningScore
    private HardSoftScore score;

    public static SupplementSchedule of(Long regimenId, String patientId,
                                        List<TimeSlot> slots, List<SupplementDose> doses) {
        return new SupplementSchedule(regimenId, patientId, slots, doses, null);
    }
}
