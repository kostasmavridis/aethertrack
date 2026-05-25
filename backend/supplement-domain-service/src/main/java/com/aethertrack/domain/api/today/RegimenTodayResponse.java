package com.aethertrack.domain.api.today;

import java.util.List;

public record RegimenTodayResponse(
        Long regimenId,
        String patientId,
        String regimenName,
        List<TodayDoseResponse> doses
) {}
