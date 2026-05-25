package com.aethertrack.domain.regimen;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "regimen")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Regimen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_id", nullable = false, length = 100)
    private String patientId;

    @Column(nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private RegimenStatus status = RegimenStatus.DRAFT;

    @Column(name = "care_plan_id", length = 100)
    private String carePlanId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "regimen", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RegimenItem> items = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public void addItem(RegimenItem item) {
        items.add(item);
        item.setRegimen(this);
    }
}
