package com.aethertrack.intake.api;

import com.aethertrack.intake.domain.AdherenceSummary;
import com.aethertrack.intake.repository.AdherenceSummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/adherence")
@RequiredArgsConstructor
public class AdherenceController {

    private final AdherenceSummaryRepository summaryRepository;

    @GetMapping
    public ResponseEntity<List<AdherenceSummary>> forPatient(
            @RequestParam String patientId) {
        return ResponseEntity.ok(
                summaryRepository.findByPatientIdOrderByEvaluatedAtDesc(patientId));
    }
}
