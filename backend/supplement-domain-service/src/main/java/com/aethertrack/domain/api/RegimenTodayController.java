package com.aethertrack.domain.api;

import com.aethertrack.domain.api.today.RegimenTodayResponse;
import com.aethertrack.domain.service.RegimenTodayQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/regimens")
@RequiredArgsConstructor
public class RegimenTodayController {

    private final RegimenTodayQueryService queryService;

    @GetMapping("/{regimenId}/today")
    public ResponseEntity<RegimenTodayResponse> today(@PathVariable Long regimenId) {
        return ResponseEntity.ok(queryService.getToday(regimenId));
    }
}
