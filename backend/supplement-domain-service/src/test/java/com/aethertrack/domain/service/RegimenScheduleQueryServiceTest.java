package com.aethertrack.domain.service;

import com.aethertrack.domain.api.schedule.RegimenScheduleResponse;
import com.aethertrack.domain.domain.ScheduledDose;
import com.aethertrack.domain.regimen.Regimen;
import com.aethertrack.domain.regimen.RegimenItem;
import com.aethertrack.domain.regimen.RegimenRepository;
import com.aethertrack.domain.repository.ScheduledDoseRepository;
import com.aethertrack.domain.supplement.Supplement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegimenScheduleQueryServiceTest {

    @Mock RegimenRepository regimenRepository;
    @Mock ScheduledDoseRepository scheduledDoseRepository;
    @InjectMocks RegimenScheduleQueryService service;

    @Test
    void builds_one_day_schedule_response() {
        Regimen regimen = new Regimen();
        regimen.setId(1L);
        regimen.setPatientId("patient-1");
        regimen.setName("Morning Stack");

        Supplement supplement = new Supplement();
        supplement.setId(1L);
        supplement.setCode("VIT-D3");
        supplement.setName("Vitamin D3");

        RegimenItem item = new RegimenItem();
        item.setId(10L);
        item.setSupplement(supplement);
        item.setDoseQty(new BigDecimal("2000"));
        item.setDoseUnit("IU");
        item.setRegimen(regimen);
        regimen.setItems(List.of(item));

        ScheduledDose dose = new ScheduledDose();
        dose.setRegimenId(1L);
        dose.setRegimenItemId(10L);
        dose.setTimeslot("MORNING");
        dose.setDayOffset(0);
        dose.setExplanation("soft score: grouped with breakfast");

        when(regimenRepository.findById(1L)).thenReturn(Optional.of(regimen));
        when(scheduledDoseRepository.findByRegimenIdAndDayOffsetOrderByRegimenItemIdAsc(1L, 0))
                .thenReturn(List.of(dose));

        RegimenScheduleResponse response = service.getOneDaySchedule(1L);

        assertThat(response.regimenId()).isEqualTo(1L);
        assertThat(response.windows()).containsExactly("MORNING", "MIDDAY", "EVENING", "NIGHT");
        assertThat(response.rows()).hasSize(1);
        assertThat(response.rows().get(0).assignments()).hasSize(4);
        assertThat(response.rows().get(0).assignments().get(0).assigned()).isTrue();
        assertThat(response.optimizationNotes()).contains("soft score: grouped with breakfast");
    }
}
