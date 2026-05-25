package com.aethertrack.domain.api.schedule;

public record ScheduleAssignmentResponse(
        String window,
        String label,
        boolean assigned
) {}
