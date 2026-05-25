package com.aethertrack.domain.api.schedule;

import java.util.List;

public record RegimenScheduleResponse(
        Long regimenId,
        String patientId,
        String regimenName,
        List<String> windows,
        List<ScheduleRowResponse> rows,
        List<String> optimizationNotes
) {}
