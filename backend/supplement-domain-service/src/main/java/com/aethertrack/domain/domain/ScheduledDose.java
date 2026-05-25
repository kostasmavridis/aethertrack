package com.aethertrack.domain.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "scheduled_dose", schema = "supplement")
@Getter
@Setter
@NoArgsConstructor
public class ScheduledDose {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "regimen_id", nullable = false)
    private Long regimenId;

    @Column(name = "regimen_item_id", nullable = false)
    private Long regimenItemId;

    @Column(name = "timeslot", nullable = false, length = 32)
    private String timeslot;

    @Column(name = "day_offset", nullable = false)
    private Integer dayOffset;

    @Column(name = "explanation", length = 255)
    private String explanation;
}
