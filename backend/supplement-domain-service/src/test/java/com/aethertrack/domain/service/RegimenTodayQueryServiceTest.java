package com.aethertrack.domain.service;

import com.aethertrack.domain.api.today.RegimenTodayResponse;
import com.aethertrack.domain.domain.Regimen;
import com.aethertrack.domain.domain.RegimenItem;
import com.aethertrack.domain.domain.ScheduledDose;
import com.aethertrack.domain.repository.RegimenRepository;
import com.aethertrack.domain.repository.ScheduledDoseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegimenTodayQueryServiceTest {

    @Mock RegimenRepository regimenRepository;
    @Mock ScheduledDoseRepository scheduledDoseRepository;
    @Mock JdbcTemplate jdbcTemplate;
    @InjectMocks RegimenTodayQueryService service;

    @Test
    void returns_today_doses_with_taken_and_adherence_status() {
        Regimen regimen = new Regimen();
        regimen.setId(1L);
        regimen.setPatientId("patient-1");
        regimen.setName("Morning Stack");

        RegimenItem item = new RegimenItem();
        item.setId(10L);
        item.setSupplementCode("VIT-D3");
        item.setDoseQty(new BigDecimal("2000"));
        item.setDoseUnit("IU");
        regimen.setItems(List.of(item));

        ScheduledDose dose = new ScheduledDose();
        dose.setRegimenId(1L);
        dose.setRegimenItemId(10L);
        dose.setTimeslot("MORNING");
        dose.setDayOffset(0);

        when(regimenRepository.findById(1L)).thenReturn(Optional.of(regimen));
        when(scheduledDoseRepository.findByRegimenIdAndDayOffsetOrderByRegimenItemIdAsc(1L, 0))
                .thenReturn(List.of(dose));
        when(jdbcTemplate.query(anyString(), any(org.springframework.jdbc.core.RowMapper.class), any()))
                .thenReturn(List.of(10L));
        when(jdbcTemplate.query(anyString(), any(org.springframework.jdbc.core.PreparedStatementSetter.class), any(org.springframework.jdbc.core.ResultSetExtractor.class)))
                .thenAnswer(inv -> java.util.Map.of(10L, "ON_TIME"));

        RegimenTodayResponse response = service.getToday(1L);

        assertThat(response.doses()).hasSize(1);
        assertThat(response.doses().get(0).taken()).isTrue();
        assertThat(response.doses().get(0).adherenceStatus()).isEqualTo("ON_TIME");
    }
}
