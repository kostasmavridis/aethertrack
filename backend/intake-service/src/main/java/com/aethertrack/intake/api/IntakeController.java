package com.aethertrack.intake.api;

import com.aethertrack.intake.domain.IntakeLog;
import com.aethertrack.intake.service.IntakeLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/intake")
@RequiredArgsConstructor
public class IntakeController {

    private final IntakeLogService intakeLogService;

    @PostMapping
    public ResponseEntity<IntakeLogResponse> create(@RequestBody @Valid IntakeLogRequest request,
                                                    @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId) {
        String effectiveCorrelationId = correlationId != null ? correlationId : UUID.randomUUID().toString();

        IntakeLog saved = intakeLogService.create(
                IntakeLog.of(request.patientId(), request.regimenItemId(), request.takenDateTime(), request.quantity()),
                effectiveCorrelationId
        );

        return ResponseEntity.ok(new IntakeLogResponse(
                saved.getId(),
                saved.getPatientId(),
                saved.getRegimenItemId(),
                saved.getTakenDateTime(),
                saved.getQuantity(),
                "RECORDED"
        ));
    }
}
