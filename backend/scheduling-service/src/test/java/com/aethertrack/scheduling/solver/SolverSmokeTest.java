package com.aethertrack.scheduling.solver;

import ai.timefold.solver.core.api.solver.Solver;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.config.solver.SolverConfig;
import ai.timefold.solver.core.config.solver.termination.TerminationConfig;
import com.aethertrack.scheduling.domain.SupplementDose;
import com.aethertrack.scheduling.domain.SupplementSchedule;
import com.aethertrack.scheduling.domain.TimeSlot;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end smoke test: 4 doses, 5 slots, 2-second solve.
 * Asserts hard score == 0 and all doses assigned. No Spring context needed.
 */
class SolverSmokeTest {

    @Test
    void solver_findsZeroHardSolution_forSimpleProblem() {
        SolverConfig config = new SolverConfig()
            .withSolutionClass(SupplementSchedule.class)
            .withEntityClasses(SupplementDose.class)
            .withConstraintProviderClass(SupplementScheduleConstraintProvider.class)
            .withTerminationConfig(new TerminationConfig().withSpentLimit(Duration.ofSeconds(2)));

        Solver<SupplementSchedule> solver = SolverFactory.<SupplementSchedule>create(config).buildSolver();

        List<TimeSlot> slots = TimeSlot.defaultSlots();

        SupplementSchedule solution = solver.solve(SupplementSchedule.of(
            1L, "patient-smoke-test", slots,
            List.of(
                buildDose(1L, "VIT-D3",   "VITAMIN", false, false),
                buildDose(2L, "VIT-C",    "VITAMIN", false, false),
                buildDose(3L, "MAG-GLY",  "MINERAL", true,  false),
                buildDose(4L, "FISH-OIL", "OMEGA",   false, true)
            )
        ));

        assertThat(solution.getScore()).isNotNull();
        assertThat(solution.getScore().hardScore()).isEqualTo(0);
        solution.getDoses().forEach(d ->
            assertThat(d.getAssignedSlot()).as("Dose %s must have slot", d.getSupplementCode()).isNotNull());

        System.out.println("=== Solver Smoke Test Result ===");
        solution.getDoses().forEach(d ->
            System.out.printf("  %-12s -> %s%n", d.getSupplementCode(), d.getAssignedSlot()));
        System.out.println("  Score: " + solution.getScore());
    }

    private SupplementDose buildDose(Long id, String code, String category,
                                     boolean nightRequired, boolean mealRequired) {
        SupplementDose d = new SupplementDose();
        d.setId(id); d.setRegimenItemId(id);
        d.setSupplementCode(code); d.setSupplementCategory(category);
        d.setDoseQty(BigDecimal.ONE); d.setDoseUnit("tablet");
        d.setNightTimeRequired(nightRequired); d.setMealRequired(mealRequired);
        return d;
    }
}
