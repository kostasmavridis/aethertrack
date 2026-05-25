package com.aethertrack.domain.regimen;

import java.util.List;

public record RegimenDto(
        Long id,
        String patientId,
        String name,
        RegimenStatus status,
        List<RegimenItemDto> items
) {
    public static RegimenDto from(Regimen regimen) {
        List<RegimenItemDto> itemDtos = regimen.getItems().stream()
                .map(RegimenItemDto::from)
                .toList();
        return new RegimenDto(
                regimen.getId(),
                regimen.getPatientId(),
                regimen.getName(),
                regimen.getStatus(),
                itemDtos
        );
    }
}
