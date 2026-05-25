package com.aethertrack.domain.service;

import com.aethertrack.domain.api.today.RegimenTodayResponse;
import com.aethertrack.domain.api.today.TodayDoseResponse;
import com.aethertrack.domain.domain.Regimen;
import com.aethertrack.domain.domain.RegimenItem;
import com.aethertrack.domain.domain.ScheduledDose;
import com.aethertrack.domain.repository.RegimenRepository;
import com.aethertrack.domain.repository.ScheduledDoseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegimenTodayQueryService {

    private final RegimenRepository regimenRepository;
    private final ScheduledDoseRepository scheduledDoseRepository;
    private final JdbcTemplate jdbcTemplate;

    public RegimenTodayResponse getToday(Long regimenId) {
        Regimen regimen = regimenRepository.findById(regimenId)
                .orElseThrow(() -> new NoSuchElementException("Regimen not found: " + regimenId));

        List<ScheduledDose> doses = scheduledDoseRepository
                .findByRegimenIdAndDayOffsetOrderByRegimenItemIdAsc(regimenId, 0);

        Set<Long> takenItemIds = new HashSet<>(jdbcTemplate.query(
                """
                SELECT DISTINCT regimen_item_id
                FROM intake.intake_log
                WHERE patient_id = ?
                  AND regimen_item_id IN (%s)
                  AND DATE(taken_date_time AT TIME ZONE 'UTC') = CURRENT_DATE
                """.formatted(idsCsv(doses)),
                (rs, rowNum) -> rs.getLong(1),
                regimen.getPatientId()
        ));

        Map<Long, String> adherenceByItem = jdbcTemplate.query(
                """
                SELECT regimen_item_id, outcome
                FROM intake.adherence_summary
                WHERE patient_id = ?
                  AND regimen_item_id IN (%s)
                ORDER BY evaluated_at DESC
                """.formatted(idsCsv(doses)),
                ps -> ps.setString(1, regimen.getPatientId()),
                rs -> {
                    Map<Long, String> map = new LinkedHashMap<>();
                    while (rs.next()) {
                        map.putIfAbsent(rs.getLong("regimen_item_id"), rs.getString("outcome"));
                    }
                    return map;
                }
        );

        Map<Long, RegimenItem> itemsById = regimen.getItems().stream()
                .collect(Collectors.toMap(RegimenItem::getId, i -> i));

        List<TodayDoseResponse> responseItems = doses.stream()
                .map(dose -> {
                    RegimenItem item = itemsById.get(dose.getRegimenItemId());
                    return new TodayDoseResponse(
                            dose.getRegimenItemId(),
                            item != null ? item.getSupplementCode() : "UNKNOWN",
                            item != null ? item.getSupplementCode() : "Unknown",
                            dose.getTimeslot(),
                            item != null ? item.getDoseQty().stripTrailingZeros().toPlainString() + " " + item.getDoseUnit() : "—",
                            takenItemIds.contains(dose.getRegimenItemId()),
                            adherenceByItem.get(dose.getRegimenItemId())
                    );
                })
                .toList();

        return new RegimenTodayResponse(
                regimen.getId(),
                regimen.getPatientId(),
                regimen.getName(),
                responseItems
        );
    }

    private String idsCsv(List<ScheduledDose> doses) {
        if (doses.isEmpty()) return "-1";
        return doses.stream()
                .map(ScheduledDose::getRegimenItemId)
                .distinct()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }
}
