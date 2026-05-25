package com.aethertrack.scheduling.service;

import ai.timefold.solver.core.api.solver.SolverManager;
import com.aethertrack.scheduling.domain.SupplementSchedule;
import com.aethertrack.scheduling.events.RegimenCreatedPayload;
import com.aethertrack.scheduling.events.RegimenCreatedPayload.RegimenItemPayload;
import com.aethertrack.scheduling.repository.RegimenItemReadModel;
import com.aethertrack.scheduling.repository.RegimenItemRepository;
import com.aethertrack.scheduling.solver.PlanningProblemMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchedulingServiceIntegrationTest {

    @Mock RegimenItemRepository regimenItemRepository;
    @Mock PlanningProblemMapper mapper;
    @Mock SolverManager<SupplementSchedule, Long> solverManager;
    @InjectMocks SchedulingService schedulingService;

    private RegimenCreatedPayload payload(Long id) {
        return new RegimenCreatedPayload(id, "p1", "Test",
            List.of(new RegimenItemPayload(1L,10L,"VIT-D3",BigDecimal.ONE,"tablet",1,null)));
    }

    @Test void fetchesDbItems_andCallsMapper() {
        var items = List.of(new RegimenItemReadModel(
            1L,42L,10L,"VIT-D3","VITAMIN",BigDecimal.ONE,"tablet",1,null,false,false));
        when(regimenItemRepository.findByRegimenId(42L)).thenReturn(items);
        when(mapper.fromReadModels(42L,"p1",items))
            .thenReturn(SupplementSchedule.of(42L,"p1",List.of(),List.of()));
        schedulingService.scheduleRegimen(payload(42L));
        verify(regimenItemRepository).findByRegimenId(42L);
        verify(mapper).fromReadModels(42L,"p1",items);
        verifyNoInteractions(solverManager);
    }

    @Test void fallsBackToPayload_whenDbEmpty() {
        when(regimenItemRepository.findByRegimenId(99L)).thenReturn(List.of());
        when(mapper.fromEventPayload(any()))
            .thenReturn(SupplementSchedule.of(99L,"p1",List.of(),List.of()));
        schedulingService.scheduleRegimen(payload(99L));
        verify(mapper).fromEventPayload(any());
        verify(mapper, never()).fromReadModels(any(),any(),any());
    }
}
