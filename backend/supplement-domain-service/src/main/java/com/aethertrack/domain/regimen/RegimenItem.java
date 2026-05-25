package com.aethertrack.domain.regimen;

import com.aethertrack.domain.supplement.Supplement;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "regimen_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegimenItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "regimen_id", nullable = false)
    private Regimen regimen;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "supplement_id", nullable = false)
    private Supplement supplement;

    @Column(name = "dose_qty", nullable = false, precision = 10, scale = 3)
    private BigDecimal doseQty;

    @Column(name = "dose_unit", nullable = false, length = 50)
    private String doseUnit;

    @Column(name = "frequency_per_day", nullable = false)
    private Integer frequencyPerDay;

    @Column(name = "schedule_window", length = 100)
    private String scheduleWindow;

    @Column(name = "nutrition_order_id", length = 100)
    private String nutritionOrderId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
