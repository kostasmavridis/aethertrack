package com.aethertrack.intake.api;

import com.aethertrack.intake.domain.IntakeLog;
import com.aethertrack.intake.service.IntakeLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IntakeController.class)
class IntakeControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean IntakeLogService intakeLogService;

    @Test
    void postIntake_returnsRecordedResponse() throws Exception {
        IntakeLog saved = IntakeLog.of("patient-1", 101L, Instant.parse("2026-05-25T04:00:00Z"), new BigDecimal("1.000"));
        saved.setId(10L);

        when(intakeLogService.create(any(IntakeLog.class), eq("corr-123"))).thenReturn(saved);

        var request = new IntakeLogRequest(
                "patient-1",
                101L,
                Instant.parse("2026-05-25T04:00:00Z"),
                new BigDecimal("1.000")
        );

        mockMvc.perform(post("/api/intake")
                        .header("X-Correlation-Id", "corr-123")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.patientId").value("patient-1"))
                .andExpect(jsonPath("$.regimenItemId").value(101L))
                .andExpect(jsonPath("$.status").value("RECORDED"));
    }
}
