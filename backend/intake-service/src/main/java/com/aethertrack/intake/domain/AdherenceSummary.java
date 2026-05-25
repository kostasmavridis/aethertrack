package com.aethertrack.intake.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Persisted adherence evaluation result for one intake event.
 *
 * outcome values:
 *   ON_TIME  – intake timestamp falls within the scheduled window
 *   EARLY    – intake is before the window start
 *   LATE     – intake is after the window end
 *   MISSED   – no intake was recorded for the window (populated by a future batch job; not this slice)
 *   UNSCHEDULED – no scheduled window found for this regimen item
 */
@Entity
@Table(name = "adherence_summary", schema = "intake")
@Getter
@Setter
@NoArgsConstructor
public class AdherenceSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_id",      nullable = false, length = 128)
    private String patientId;

    @Column(name = "regimen_item_id", nullable = false)
    private Long regimenItemId;

    @Column(name = "intake_log_id",   nullable = false)
    private Long intakeLogId;

    @Column(name = "outcome",         nullable = false, length = 16)
    private String outcome;

    @Column(name = "deviation_mins")
    private Integer deviationMins;

    @Column(name = "evaluated_at",    nullable = false)
    private Instant evaluatedAt = Instant.now();

    public static AdherenceSummary of(String patientId, Long regimenItemId,
                                      Long intakeLogId, String outcome,
                                      Integer deviationMins) {
        AdherenceSummary s = new AdherenceSummary();
        s.setPatientId(patientId);
        s.setRegimenItemId(regimenItemId);
        s.setIntakeLogId(intakeLogId);
        s.setOutcome(outcome);
        s.setDeviationMins(deviationMins);
        return s;
    }
}
