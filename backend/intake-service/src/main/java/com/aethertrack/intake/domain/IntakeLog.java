package com.aethertrack.intake.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "intake_log", schema = "intake")
@Getter
@Setter
@NoArgsConstructor
public class IntakeLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_id", nullable = false, length = 128)
    private String patientId;

    @Column(name = "regimen_item_id", nullable = false)
    private Long regimenItemId;

    @Column(name = "taken_date_time", nullable = false)
    private Instant takenDateTime;

    @Column(name = "quantity", nullable = false, precision = 12, scale = 3)
    private BigDecimal quantity;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public static IntakeLog of(String patientId, Long regimenItemId, Instant takenDateTime, BigDecimal quantity) {
        IntakeLog log = new IntakeLog();
        log.setPatientId(patientId);
        log.setRegimenItemId(regimenItemId);
        log.setTakenDateTime(takenDateTime);
        log.setQuantity(quantity);
        return log;
    }
}
