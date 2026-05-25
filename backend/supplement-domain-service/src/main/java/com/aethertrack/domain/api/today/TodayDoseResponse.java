package com.aethertrack.domain.api.today;

public record TodayDoseResponse(
        Long regimenItemId,
        String supplementCode,
        String supplementName,
        String window,
        String doseLabel,
        boolean taken,
        String adherenceStatus
) {}
