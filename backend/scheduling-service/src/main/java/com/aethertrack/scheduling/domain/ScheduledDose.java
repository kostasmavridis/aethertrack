package com.aethertrack.scheduling.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalTime;

/**
 * JPA entity persisted after each Timefold solve.
 * One row = one SupplementDose assignment (regimenItemId → TimeSlot).
 */
@Entity
@Table(name = "scheduled_dose", schema = "scheduling",
       indexes = {
           @Index(name = "idx_sd_regimen_id",  columnList = "regimen_id"),
           @Index(name = "idx_sd_regimen_item", columnList = "regimen_item_id")
       })
@Getter
@Setter
@NoArgsConstructor
public class ScheduledDose {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "regimen_id",      nullable = false)
    private Long regimenId;

    @Column(name = "regimen_item_id", nullable = false)
    private Long regimenItemId;

    @Column(name = "timeslot",        nullable = false, length = 32)
    private String timeslot;

    @Column(name = "timeslot_start",  nullable = false)
    private LocalTime timeslotStart;

    @Column(name = "timeslot_end",    nullable = false)
    private LocalTime timeslotEnd;

    @Column(name = "day_offset",      nullable = false)
    private int dayOffset = 0;

    @Column(name = "hard_score",      nullable = false)
    private int hardScore;

    @Column(name = "soft_score",      nullable = false)
    private int softScore;

    @Column(name = "created_at",      nullable = false)
    private Instant createdAt = Instant.now();

    public static ScheduledDose from(Long regimenId, SupplementDose dose,
                                     int hardScore, int softScore) {
        ScheduledDose sd = new ScheduledDose();
        sd.setRegimenId(regimenId);
        sd.setRegimenItemId(dose.getRegimenItemId());
        sd.setTimeslot(dose.getAssignedSlot().getId());
        sd.setTimeslotStart(dose.getAssignedSlot().getStartTime());
        sd.setTimeslotEnd(dose.getAssignedSlot().getEndTime());
        sd.setDayOffset(0);
        sd.setHardScore(hardScore);
        sd.setSoftScore(softScore);
        return sd;
    }
}
