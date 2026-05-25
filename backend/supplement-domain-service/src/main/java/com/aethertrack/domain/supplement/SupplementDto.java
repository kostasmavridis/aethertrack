package com.aethertrack.domain.supplement;

import java.util.List;

/**
 * Outbound DTO – safe to serialize and expose via REST.
 * Deliberately excludes internal columns (createdAt, updatedAt).
 */
public record SupplementDto(
        Long id,
        String code,
        String name,
        String category,
        String description,
        List<Supplement.NutrientEntry> nutrients
) {
    public static SupplementDto from(Supplement s) {
        return new SupplementDto(
                s.getId(),
                s.getCode(),
                s.getName(),
                s.getCategory(),
                s.getDescription(),
                s.getNutrients()
        );
    }
}
