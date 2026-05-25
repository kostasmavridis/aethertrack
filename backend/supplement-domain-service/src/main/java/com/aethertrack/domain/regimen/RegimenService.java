package com.aethertrack.domain.regimen;

import com.aethertrack.domain.supplement.Supplement;
import com.aethertrack.domain.supplement.SupplementRepository;
import com.aethertrack.events.RegimenCreatedPayload;
import com.aethertrack.events.RegimenEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegimenService {

    private final RegimenRepository regimenRepository;
    private final SupplementRepository supplementRepository;
    private final RegimenEventPublisher eventPublisher;

    @Transactional
    public RegimenDto create(RegimenCreateRequest request) {
        List<Long> requestedIds = request.items().stream()
                .map(RegimenItemCreateRequest::supplementId)
                .toList();
        List<Supplement> supplements = supplementRepository.findAllById(requestedIds);
        Map<Long, Supplement> supplementsById = supplements.stream()
                .collect(Collectors.toMap(Supplement::getId, s -> s));

        if (supplementsById.size() != requestedIds.stream().distinct().count()) {
            Set<Long> missing = requestedIds.stream()
                    .filter(id -> !supplementsById.containsKey(id))
                    .collect(Collectors.toSet());
            throw new UnknownSupplementException("Unknown supplement IDs: " + missing);
        }

        Regimen regimen = Regimen.builder()
                .patientId(request.patientId())
                .name(request.name())
                .status(RegimenStatus.DRAFT)
                .build();

        request.items().forEach(itemReq -> {
            Supplement supplement = supplementsById.get(itemReq.supplementId());
            RegimenItem item = RegimenItem.builder()
                    .supplement(supplement)
                    .doseQty(itemReq.doseQty())
                    .doseUnit(itemReq.doseUnit())
                    .frequencyPerDay(itemReq.frequencyPerDay())
                    .scheduleWindow(itemReq.scheduleWindow())
                    .build();
            regimen.addItem(item);
        });

        Regimen saved = regimenRepository.save(regimen);
        RegimenDto dto = RegimenDto.from(saved);

        // Build event payload and publish (best-effort; Slice 5 adds outbox)
        List<RegimenCreatedPayload.RegimenItemPayload> itemPayloads = saved.getItems().stream()
                .map(i -> new RegimenCreatedPayload.RegimenItemPayload(
                        i.getId(),
                        i.getSupplement().getId(),
                        i.getSupplement().getCode(),
                        i.getDoseQty(),
                        i.getDoseUnit(),
                        i.getFrequencyPerDay(),
                        i.getScheduleWindow()
                ))
                .toList();

        eventPublisher.publishRegimenCreated(new RegimenCreatedPayload(
                saved.getId(), saved.getPatientId(), saved.getName(), itemPayloads));

        return dto;
    }
}
