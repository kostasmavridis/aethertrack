package com.aethertrack.domain.supplement;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "supplement")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Supplement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String code;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * JSONB column – e.g. [{"substance":"Iron","amount":50,"unit":"mg"}]
     * Mapped as List<NutrientEntry> via Hibernate's JSON type.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<NutrientEntry> nutrients;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

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

    // ── inner value object ────────────────────────────────────────────────────
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class NutrientEntry {
        private String substance;
        private Double amount;
        private String unit;
    }
}
