package com.aethertrack.intake.repository;

import com.aethertrack.intake.domain.AdherenceSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdherenceSummaryRepository extends JpaRepository<AdherenceSummary, Long> {
    List<AdherenceSummary> findByPatientIdOrderByEvaluatedAtDesc(String patientId);
}
