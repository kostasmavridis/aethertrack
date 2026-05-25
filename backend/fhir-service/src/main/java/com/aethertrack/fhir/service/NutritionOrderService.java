package com.aethertrack.fhir.service;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.aethertrack.fhir.config.FhirProperties;
import com.aethertrack.fhir.domain.RegimenFhirMapping;
import com.aethertrack.fhir.events.RegimenCreatedPayload;
import com.aethertrack.fhir.mapper.NutritionOrderMapper;
import com.aethertrack.fhir.repository.RegimenFhirMappingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r5.model.NutritionOrder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestrates: map payload → NutritionOrder → HAPI create → persist mapping.
 *
 * Idempotent: if a mapping already exists for the given regimenId the method
 * returns immediately without hitting HAPI, so Kafka re-deliveries are safe.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NutritionOrderService {

    private final NutritionOrderMapper          mapper;
    private final IGenericClient                fhirClient;
    private final FhirProperties                props;
    private final RegimenFhirMappingRepository  mappingRepo;

    @Transactional
    public void createFromRegimen(RegimenCreatedPayload payload, String correlationId) {
        Long regimenId = payload.regimenId();

        // ── Idempotency guard ─────────────────────────────────────────────
        if (mappingRepo.existsByRegimenId(regimenId)) {
            log.info("[NutritionOrderService] Mapping already exists for regimenId={} – skipping",
                     regimenId);
            return;
        }

        // ── Map payload → FHIR NutritionOrder ────────────────────────────
        NutritionOrder no = mapper.toNutritionOrder(payload);
        log.info("[NutritionOrderService] Creating NutritionOrder regimenId={} items={}",
                 regimenId, payload.items().size());

        // ── Create on HAPI server ─────────────────────────────────────────
        MethodOutcome outcome = fhirClient
            .create()
            .resource(no)
            .execute();

        String fhirId  = outcome.getId().getIdPart();
        String fhirUrl = props.baseUrl() + "/NutritionOrder/" + fhirId;

        log.info("[NutritionOrderService] Created NutritionOrder id={} url={} regimenId={}",
                 fhirId, fhirUrl, regimenId);

        // ── Persist mapping (same transaction) ────────────────────────────
        mappingRepo.save(RegimenFhirMapping.of(regimenId, fhirId, fhirUrl));
        log.info("[NutritionOrderService] Mapping persisted regimenId={} ↔ NutritionOrder/{}",
                 regimenId, fhirId);
    }
}
