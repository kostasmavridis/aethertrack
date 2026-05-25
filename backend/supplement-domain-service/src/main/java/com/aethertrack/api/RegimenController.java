package com.aethertrack.api;

import com.aethertrack.domain.regimen.RegimenCreateRequest;
import com.aethertrack.domain.regimen.RegimenDto;
import com.aethertrack.domain.regimen.RegimenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/regimens")
@RequiredArgsConstructor
public class RegimenController {

    private final RegimenService regimenService;

    /**
     * POST /api/regimens
     *
     * Creates a new regimen in DRAFT status with the given items.
     * Returns 201 Created with the persisted RegimenDto (includes generated IDs).
     * Returns 400 Bad Request if:
     *   - Bean Validation fails (missing/invalid fields)
     *   - Any supplementId does not exist in the supplement table
     */
    @PostMapping
    public ResponseEntity<RegimenDto> create(@RequestBody @Valid RegimenCreateRequest request) {
        RegimenDto dto = regimenService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }
}
