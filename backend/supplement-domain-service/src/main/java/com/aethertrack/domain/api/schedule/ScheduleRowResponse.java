package com.aethertrack.domain.api.schedule;

import java.util.List;

public record ScheduleRowResponse(
        Long regimenItemId,
        String supplementCode,
        String supplementName,
        List<ScheduleAssignmentResponse> assignments
) {}
