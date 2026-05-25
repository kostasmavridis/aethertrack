package com.aethertrack.scheduling.solver;

import com.aethertrack.scheduling.domain.SupplementDose;
import com.aethertrack.scheduling.domain.SupplementSchedule;
import com.aethertrack.scheduling.events.RegimenCreatedPayload;
import com.aethertrack.scheduling.events.RegimenCreatedPayload.RegimenItemPayload;
import com.aethertrack.scheduling.repository.RegimenItemReadModel;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class PlanningProblemMapperTest {

    private final PlanningProblemMapper mapper = new PlanningProblemMapper();

    @Test void fromReadModels_expandsFrequencyPerDay() {
        var item = new RegimenItemReadModel(1L,10L,100L,"VIT-D3","VITAMIN",BigDecimal.ONE,"IU",2,null,false,false);
        assertThat(mapper.fromReadModels(10L,"p1",List.of(item)).getDoses()).hasSize(2);
    }

    @Test void fromReadModels_setsNightTimeRequired() {
        var item = new RegimenItemReadModel(2L,10L,101L,"MAG-GLY","MINERAL",BigDecimal.ONE,"mg",1,"NIGHT",true,false);
        assertThat(mapper.fromReadModels(10L,"p1",List.of(item)).getDoses().get(0).isNightTimeRequired()).isTrue();
    }

    @Test void fromReadModels_returnsDefaultSlots() {
        var item = new RegimenItemReadModel(3L,10L,102L,"VIT-C","VITAMIN",BigDecimal.ONE,"tablet",1,null,false,false);
        assertThat(mapper.fromReadModels(10L,"p1",List.of(item)).getTimeSlots()).hasSize(5);
    }

    @Test void fromEventPayload_derivesNightRequired() {
        var payload = new RegimenCreatedPayload(5L,"p2","T",
            List.of(new RegimenItemPayload(1L,200L,"MAG-GLY",BigDecimal.ONE,"tablet",1,"NIGHT")));
        assertThat(mapper.fromEventPayload(payload).getDoses().get(0).isNightTimeRequired()).isTrue();
    }

    @Test void fromEventPayload_derivesMealRequired() {
        var payload = new RegimenCreatedPayload(6L,"p3","T",
            List.of(new RegimenItemPayload(2L,201L,"FISH-OIL",BigDecimal.ONE,"softgel",1,"WITH_MEAL")));
        assertThat(mapper.fromEventPayload(payload).getDoses().get(0).isMealRequired()).isTrue();
    }

    @Test void fromEventPayload_assignsUniqueIds() {
        var payload = new RegimenCreatedPayload(7L,"p4","T",List.of(
            new RegimenItemPayload(1L,200L,"VIT-C",BigDecimal.ONE,"tablet",2,null),
            new RegimenItemPayload(2L,201L,"VIT-D3",BigDecimal.ONE,"tablet",1,null)));
        var doses = mapper.fromEventPayload(payload).getDoses();
        assertThat(doses.stream().map(SupplementDose::getId).toList()).doesNotHaveDuplicates();
        assertThat(doses).hasSize(3);
    }
}
