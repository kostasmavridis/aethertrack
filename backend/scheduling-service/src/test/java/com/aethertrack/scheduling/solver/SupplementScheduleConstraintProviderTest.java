package com.aethertrack.scheduling.solver;

import ai.timefold.solver.test.api.score.stream.ConstraintVerifier;
import com.aethertrack.scheduling.domain.SupplementDose;
import com.aethertrack.scheduling.domain.SupplementSchedule;
import com.aethertrack.scheduling.domain.TimeSlot;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalTime;

/**
 * Timefold ConstraintVerifier unit tests – no solver run, no Spring context.
 */
class SupplementScheduleConstraintProviderTest {

    private static final TimeSlot MORNING   = TimeSlot.of("MORNING",   LocalTime.of( 7, 0), LocalTime.of( 9, 0), true,  false);
    private static final TimeSlot MIDDAY    = TimeSlot.of("MIDDAY",    LocalTime.of(12, 0), LocalTime.of(14, 0), true,  false);
    private static final TimeSlot AFTERNOON = TimeSlot.of("AFTERNOON", LocalTime.of(15, 0), LocalTime.of(17, 0), false, false);
    private static final TimeSlot NIGHT     = TimeSlot.of("NIGHT",     LocalTime.of(21, 0), LocalTime.of(23, 0), false, true);

    private final ConstraintVerifier<SupplementScheduleConstraintProvider, SupplementSchedule> verifier =
        ConstraintVerifier.build(new SupplementScheduleConstraintProvider(),
                                 SupplementSchedule.class, SupplementDose.class);

    private SupplementDose dose(Long id, String code, String category,
                                boolean nightRequired, boolean mealRequired, TimeSlot slot) {
        SupplementDose d = new SupplementDose();
        d.setId(id); d.setRegimenItemId(id);
        d.setSupplementCode(code); d.setSupplementCategory(category);
        d.setDoseQty(BigDecimal.ONE); d.setDoseUnit("tablet");
        d.setNightTimeRequired(nightRequired); d.setMealRequired(mealRequired);
        d.setAssignedSlot(slot);
        return d;
    }

    @Test void h1_sameSuppSameSlot_penalisesOneHard() {
        verifier.verifyThat(SupplementScheduleConstraintProvider::noConflictingSameSlot)
                .given(dose(1L,"VIT-D3","VITAMIN",false,false,MORNING),
                       dose(2L,"VIT-D3","VITAMIN",false,false,MORNING))
                .penalizesBy(1);
    }
    @Test void h1_sameSuppDifferentSlots_noPenalty() {
        verifier.verifyThat(SupplementScheduleConstraintProvider::noConflictingSameSlot)
                .given(dose(1L,"VIT-D3","VITAMIN",false,false,MORNING),
                       dose(2L,"VIT-D3","VITAMIN",false,false,MIDDAY))
                .penalizesBy(0);
    }
    @Test void h2_nightRequiredInNonNightSlot_penalisesOneHard() {
        verifier.verifyThat(SupplementScheduleConstraintProvider::nightTimeRequiredDoseInNightSlot)
                .given(dose(1L,"MAG-GLY","MINERAL",true,false,MORNING))
                .penalizesBy(1);
    }
    @Test void h2_nightRequiredInNightSlot_noPenalty() {
        verifier.verifyThat(SupplementScheduleConstraintProvider::nightTimeRequiredDoseInNightSlot)
                .given(dose(1L,"MAG-GLY","MINERAL",true,false,NIGHT))
                .penalizesBy(0);
    }
    @Test void h3_mealRequiredInNonMealSlot_penalisesOneHard() {
        verifier.verifyThat(SupplementScheduleConstraintProvider::mealRequiredDoseInMealSlot)
                .given(dose(1L,"FISH-OIL","OMEGA",false,true,AFTERNOON))
                .penalizesBy(1);
    }
    @Test void h3_mealRequiredInMealSlot_noPenalty() {
        verifier.verifyThat(SupplementScheduleConstraintProvider::mealRequiredDoseInMealSlot)
                .given(dose(1L,"FISH-OIL","OMEGA",false,true,MORNING))
                .penalizesBy(0);
    }
    @Test void s1_twoDosesInSameSlot_penalisesOnce() {
        verifier.verifyThat(SupplementScheduleConstraintProvider::minimiseDistinctUsedSlots)
                .given(dose(1L,"VIT-C","VITAMIN",false,false,MORNING),
                       dose(2L,"ZINC","MINERAL",false,false,MORNING))
                .penalizesBy(1);
    }
    @Test void s1_twoDosesInDifferentSlots_penalisesTwice() {
        verifier.verifyThat(SupplementScheduleConstraintProvider::minimiseDistinctUsedSlots)
                .given(dose(1L,"VIT-C","VITAMIN",false,false,MORNING),
                       dose(2L,"ZINC","MINERAL",false,false,MIDDAY))
                .penalizesBy(2);
    }
}
