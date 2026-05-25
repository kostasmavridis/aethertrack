package com.aethertrack.domain.repository;

import com.aethertrack.domain.domain.ScheduledDose;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduledDoseRepository extends JpaRepository<ScheduledDose, Long> {
    List<ScheduledDose> findByRegimenIdAndDayOffsetOrderByRegimenItemIdAsc(Long regimenId, Integer dayOffset);
}
