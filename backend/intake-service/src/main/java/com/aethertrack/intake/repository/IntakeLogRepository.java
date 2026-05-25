package com.aethertrack.intake.repository;

import com.aethertrack.intake.domain.IntakeLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IntakeLogRepository extends JpaRepository<IntakeLog, Long> {
}
