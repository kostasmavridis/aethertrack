package com.aethertrack.domain.api;

import com.aethertrack.domain.api.schedule.RegimenScheduleResponse;
import com.aethertrack.domain.service.RegimenScheduleQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/regimens")
@RequiredArgsConstructor
public class RegimenScheduleController {

    private final RegimenScheduleQueryService queryService;

    @GetMapping("/{regimenId}/schedule")
    public ResponseEntity<RegimenScheduleResponse> schedule(@PathVariable Long regimenId) {
        return ResponseEntity.ok(queryService.getOneDaySchedule(regimenId));
    }
}
