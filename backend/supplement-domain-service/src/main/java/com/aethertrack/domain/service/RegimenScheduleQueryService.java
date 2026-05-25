package com.aethertrack.domain.service;

import com.aethertrack.domain.api.schedule.RegimenScheduleResponse;
import com.aethertrack.domain.api.schedule.ScheduleAssignmentResponse;
import com.aethertrack.domain.api.schedule.ScheduleRowResponse;
import com.aethertrack.domain.domain.Regimen;
import com.aethertrack.domain.domain.RegimenItem;
import com.aethertrack.domain.domain.ScheduledDose;
import com.aethertrack.domain.repository.RegimenRepository;
import com.aethertrack.domain.repository.ScheduledDoseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegimenScheduleQueryService {

    private static final List<String> DEFAULT_WINDOWS = List.of("MORNING", "MIDDAY", "EVENING", "NIGHT");

    private final RegimenRepository regimenRepository;
    private final ScheduledDoseRepository scheduledDoseRepository;

    public RegimenScheduleResponse getOneDaySchedule(Long regimenId) {
        Regimen regimen = regimenRepository.findById(regimenId)
                .orElseThrow(() -> new NoSuchElementException("Regimen not found: " + regimenId));

        List<ScheduledDose> doses = scheduledDoseRepository
                .findByRegimenIdAndDayOffsetOrderByRegimenItemIdAsc(regimenId, 0);

        Set<String> notes = doses.stream()
                .map(ScheduledDose::getExplanation)
                .filter(Objects::nonNull)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Map<Long, List<ScheduledDose>> byItem = doses.stream()
                .collect(Collectors.groupingBy(ScheduledDose::getRegimenItemId, LinkedHashMap::new, Collectors.toList()));

        List<ScheduleRowResponse> rows = regimen.getItems().stream()
                .map(item -> toRow(item, byItem.getOrDefault(item.getId(), List.of())))
                .toList();

        return new RegimenScheduleResponse(
                regimen.getId(),
                regimen.getPatientId(),
                regimen.getName(),
                DEFAULT_WINDOWS,
                rows,
                new ArrayList<>(notes)
        );
    }

    private ScheduleRowResponse toRow(RegimenItem item, List<ScheduledDose> itemDoses) {
        Map<String, ScheduledDose> assignments = itemDoses.stream()
                .collect(Collectors.toMap(ScheduledDose::getTimeslot, d -> d, (a, b) -> a, LinkedHashMap::new));

        List<ScheduleAssignmentResponse> cells = DEFAULT_WINDOWS.stream()
                .map(window -> {
                    ScheduledDose dose = assignments.get(window);
                    return new ScheduleAssignmentResponse(
                            window,
                            dose != null ? formatLabel(item, dose) : "—",
                            dose != null
                    );
                })
                .toList();

        return new ScheduleRowResponse(
                item.getId(),
                item.getSupplementCode(),
                item.getSupplementCode(),
                cells
        );
    }

    private String formatLabel(RegimenItem item, ScheduledDose dose) {
        return item.getDoseQty().stripTrailingZeros().toPlainString()
                + " " + item.getDoseUnit()
                + (dose.getExplanation() != null && !dose.getExplanation().isBlank() ? " · " + dose.getExplanation() : "");
    }
}
