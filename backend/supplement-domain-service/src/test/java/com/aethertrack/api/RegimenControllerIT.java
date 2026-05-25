package com.aethertrack.api;

import com.aethertrack.domain.supplement.Supplement;
import com.aethertrack.domain.supplement.SupplementRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class RegimenControllerIT {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("aethertrack_test")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",      postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired MockMvc mockMvc;
    @Autowired SupplementRepository supplementRepository;

    @Test
    void createRegimen_persistsAndReturns201() throws Exception {
        List<Supplement> supplements = supplementRepository.findAll();
        assertThat(supplements).isNotEmpty();
        Long supplementId = supplements.get(0).getId();

        String payload = """
                {
                  "patientId": "patient-123",
                  "name": "Morning stack",
                  "items": [
                    {
                      "supplementId": %d,
                      "doseQty": 1.0,
                      "doseUnit": "tablet",
                      "frequencyPerDay": 1,
                      "scheduleWindow": "MORNING"
                    }
                  ]
                }
                """.formatted(supplementId);

        mockMvc.perform(post("/api/regimens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.patientId").value("patient-123"))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].supplementId").value(supplementId))
                .andExpect(jsonPath("$.items[0].doseUnit").value("tablet"));
    }

    @Test
    void createRegimen_unknownSupplementId_returns400() throws Exception {
        String payload = """
                {
                  "patientId": "patient-456",
                  "name": "Bad stack",
                  "items": [
                    {
                      "supplementId": 99999,
                      "doseQty": 1.0,
                      "doseUnit": "capsule",
                      "frequencyPerDay": 1
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/regimens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createRegimen_missingPatientId_returns400() throws Exception {
        String payload = """
                {
                  "name": "No patient",
                  "items": [
                    {
                      "supplementId": 1,
                      "doseQty": 1.0,
                      "doseUnit": "tablet",
                      "frequencyPerDay": 1
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/regimens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }
}
