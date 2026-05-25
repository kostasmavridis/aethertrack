package com.aethertrack.intake.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

/**
 * Lightweight local copy of the scheduled dose window for a regimen item.
 * Populated when OptimizationCompleted events arrive (future slice) or seeded
 * from the scheduling-service REST read for now.
 *
 * window_start_time / window_end_time are local-clock times (no timezone)
 * because a supplement window is the same regardless of DST.
 */
@Entity
@Table(name = "scheduled_dose_ref", schema = "intake")
@Getter
@Setter
@NoArgsConstructor
public class ScheduledDoseRef {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "regimen_item_id", nullable = false)
    private Long regimenItemId;

    @Column(name = "patient_id", nullable = false, length = 128)
    private String patientId;

    @Column(name = "timeslot_code", nullable = false, length = 32)
    private String timeslotCode;

    @Column(name = "window_start_time", nullable = false)
    private LocalTime windowStartTime;

    @Column(name = "window_end_time", nullable = false)
    private LocalTime windowEndTime;

    public static ScheduledDoseRef of(Long regimenItemId, String patientId,
                                      String timeslotCode,
                                      LocalTime start, LocalTime end) {
        ScheduledDoseRef ref = new ScheduledDoseRef();
        ref.setRegimenItemId(regimenItemId);
        ref.setPatientId(patientId);
        ref.setTimeslotCode(timeslotCode);
        ref.setWindowStartTime(start);
        ref.setWindowEndTime(end);
        return ref;
    }
}
