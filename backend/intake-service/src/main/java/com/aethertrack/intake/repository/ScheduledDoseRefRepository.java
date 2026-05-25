package com.aethertrack.intake.repository;

import com.aethertrack.intake.domain.ScheduledDoseRef;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ScheduledDoseRefRepository extends JpaRepository<ScheduledDoseRef, Long> {
    Optional<ScheduledDoseRef> findFirstByRegimenItemId(Long regimenItemId);
}
