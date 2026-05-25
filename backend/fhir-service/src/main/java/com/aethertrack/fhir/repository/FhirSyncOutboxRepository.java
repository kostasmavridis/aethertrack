package com.aethertrack.fhir.repository;

import com.aethertrack.fhir.domain.FhirSyncOutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FhirSyncOutboxRepository extends JpaRepository<FhirSyncOutboxEvent, Long> {
    List<FhirSyncOutboxEvent> findTop20ByStatusOrderByCreatedAtAsc(String status);
}
