package com.aethertrack.fhir.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Persists the regimen_id ↔ FHIR NutritionOrder logical-ID mapping.
 * One row per regimen; updated in Slice 12 when Timing is refreshed.
 */
@Entity
@Table(name = "regimen_fhir_mapping", schema = "fhir",
       uniqueConstraints = @UniqueConstraint(columnNames = "regimen_id"))
@Getter @Setter @NoArgsConstructor
public class RegimenFhirMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "regimen_id",         nullable = false, unique = true)
    private Long regimenId;

    @Column(name = "nutrition_order_id", nullable = false, length = 64)
    private String nutritionOrderId;       // FHIR logical ID returned by HAPI

    @Column(name = "nutrition_order_url", length = 256)
    private String nutritionOrderUrl;      // full URL, e.g. http://hapi:8080/fhir/NutritionOrder/42

    @Column(name = "fhir_version",       nullable = false, length = 16)
    private String fhirVersion = "5.0.0";

    @Column(name = "created_at",         nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at",         nullable = false)
    private Instant updatedAt = Instant.now();

    public static RegimenFhirMapping of(Long regimenId,
                                        String nutritionOrderId,
                                        String nutritionOrderUrl) {
        RegimenFhirMapping m = new RegimenFhirMapping();
        m.setRegimenId(regimenId);
        m.setNutritionOrderId(nutritionOrderId);
        m.setNutritionOrderUrl(nutritionOrderUrl);
        return m;
    }
}
