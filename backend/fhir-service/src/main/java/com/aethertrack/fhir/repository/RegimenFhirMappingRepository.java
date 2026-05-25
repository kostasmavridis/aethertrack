package com.aethertrack.fhir.repository;

import com.aethertrack.fhir.domain.RegimenFhirMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RegimenFhirMappingRepository extends JpaRepository<RegimenFhirMapping, Long> {

    Optional<RegimenFhirMapping> findByRegimenId(Long regimenId);

    boolean existsByRegimenId(Long regimenId);
}
