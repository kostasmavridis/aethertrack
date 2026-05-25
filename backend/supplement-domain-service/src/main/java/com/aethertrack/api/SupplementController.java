package com.aethertrack.api;

import com.aethertrack.domain.supplement.SupplementDto;
import com.aethertrack.domain.supplement.SupplementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/supplements")
@RequiredArgsConstructor
public class SupplementController {

    private final SupplementService supplementService;

    /**
     * GET /api/supplements
     * Returns all active supplements; optionally filtered by category.
     *
     * Query params:
     *   category  – e.g. ?category=MINERAL
     */
    @GetMapping
    public ResponseEntity<List<SupplementDto>> getAll(
            @RequestParam(required = false) String category) {

        List<SupplementDto> result = (category != null && !category.isBlank())
                ? supplementService.findByCategory(category)
                : supplementService.findAll();

        return ResponseEntity.ok(result);
    }
}
