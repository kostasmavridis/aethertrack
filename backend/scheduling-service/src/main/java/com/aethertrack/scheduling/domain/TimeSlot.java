package com.aethertrack.scheduling.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

/**
 * A discrete intake window within a day (problem fact / planning value).
 * TimeSlots are immutable; no Timefold annotations needed here.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlot {

    private String id;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean withMeal;
    private boolean nightTime;

    public static TimeSlot of(String id, LocalTime start, LocalTime end,
                               boolean withMeal, boolean nightTime) {
        return new TimeSlot(id, start, end, withMeal, nightTime);
    }

    /** Standard daily slot set used when no user preferences are available. */
    public static List<TimeSlot> defaultSlots() {
        return List.of(
            of("MORNING",   LocalTime.of( 7, 0), LocalTime.of( 9, 0), true,  false),
            of("MIDDAY",    LocalTime.of(12, 0), LocalTime.of(14, 0), true,  false),
            of("AFTERNOON", LocalTime.of(15, 0), LocalTime.of(17, 0), false, false),
            of("EVENING",   LocalTime.of(18, 0), LocalTime.of(20, 0), true,  false),
            of("NIGHT",     LocalTime.of(21, 0), LocalTime.of(23, 0), false, true)
        );
    }

    @Override
    public String toString() {
        return id + "(" + startTime + "-" + endTime + ")";
    }
}
