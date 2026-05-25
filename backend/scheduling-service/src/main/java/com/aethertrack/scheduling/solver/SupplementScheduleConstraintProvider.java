package com.aethertrack.scheduling.solver;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintCollectors;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.Joiners;
import com.aethertrack.scheduling.domain.SupplementDose;

/**
 * Timefold constraint definitions for supplement scheduling.
 *
 * Hard:
 *   H1 – No duplicate supplements in the same slot
 *   H2 – Night-time required doses must land in a night-time slot
 *   H3 – Meal-required doses must land in a with-meal slot
 *
 * Soft:
 *   S1 – Minimise number of distinct used time slots (reduce pill burden)
 *   S2 – Prefer earlier slots for non-night supplements
 */
public class SupplementScheduleConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory factory) {
        return new Constraint[]{
            noConflictingSameSlot(factory),
            nightTimeRequiredDoseInNightSlot(factory),
            mealRequiredDoseInMealSlot(factory),
            minimiseDistinctUsedSlots(factory),
            preferEarlierSlots(factory)
        };
    }

    Constraint noConflictingSameSlot(ConstraintFactory factory) {
        return factory.forEachUniquePair(
                SupplementDose.class,
                Joiners.equal(SupplementDose::getSupplementCode),
                Joiners.equal(SupplementDose::getAssignedSlot))
            .penalize(HardSoftScore.ONE_HARD)
            .asConstraint("No duplicate supplement in same slot");
    }

    Constraint nightTimeRequiredDoseInNightSlot(ConstraintFactory factory) {
        return factory.forEach(SupplementDose.class)
            .filter(d -> d.isNightTimeRequired()
                      && d.getAssignedSlot() != null
                      && !d.getAssignedSlot().isNightTime())
            .penalize(HardSoftScore.ONE_HARD)
            .asConstraint("Night-time required dose must be in night slot");
    }

    Constraint mealRequiredDoseInMealSlot(ConstraintFactory factory) {
        return factory.forEach(SupplementDose.class)
            .filter(d -> d.isMealRequired()
                      && d.getAssignedSlot() != null
                      && !d.getAssignedSlot().isWithMeal())
            .penalize(HardSoftScore.ONE_HARD)
            .asConstraint("Meal-required dose must be in meal slot");
    }

    Constraint minimiseDistinctUsedSlots(ConstraintFactory factory) {
        return factory.forEach(SupplementDose.class)
            .filter(d -> d.getAssignedSlot() != null)
            .groupBy(SupplementDose::getAssignedSlot, ConstraintCollectors.count())
            .filter((slot, count) -> count >= 1)
            .penalize(HardSoftScore.ONE_SOFT, (slot, count) -> 1)
            .asConstraint("Minimise distinct used slots");
    }

    Constraint preferEarlierSlots(ConstraintFactory factory) {
        return factory.forEach(SupplementDose.class)
            .filter(d -> !d.isNightTimeRequired() && d.getAssignedSlot() != null)
            .penalize(HardSoftScore.ONE_SOFT,
                      d -> d.getAssignedSlot().getStartTime().getHour())
            .asConstraint("Prefer earlier slots for non-night supplements");
    }
}
