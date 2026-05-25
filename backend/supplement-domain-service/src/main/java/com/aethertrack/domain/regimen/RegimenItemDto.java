package com.aethertrack.domain.regimen;

import java.math.BigDecimal;

public record RegimenItemDto(
        Long id,
        Long supplementId,
        BigDecimal doseQty,
        String doseUnit,
        Integer frequencyPerDay,
        String scheduleWindow
) {
    public static RegimenItemDto from(RegimenItem item) {
        return new RegimenItemDto(
                item.getId(),
                item.getSupplement().getId(),
                item.getDoseQty(),
                item.getDoseUnit(),
                item.getFrequencyPerDay(),
                item.getScheduleWindow()
        );
    }
}
