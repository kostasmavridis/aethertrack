package com.aethertrack.domain.regimen;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record RegimenCreateRequest(
        @NotBlank(message = "patientId is required")
        String patientId,

        @NotBlank(message = "name is required")
        String name,

        @NotEmpty(message = "items list must not be empty")
        List<@Valid RegimenItemCreateRequest> items
) {
}
