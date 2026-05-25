package com.aethertrack.scheduling.repository;

import com.aethertrack.scheduling.domain.ScheduledDose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ScheduledDoseRepository extends JpaRepository<ScheduledDose, Long> {

    List<ScheduledDose> findByRegimenId(Long regimenId);

    /** Delete previous schedule rows before persisting a re-solved regimen. */
    @Modifying
    @Query("DELETE FROM ScheduledDose sd WHERE sd.regimenId = :regimenId")
    int deleteByRegimenId(Long regimenId);
}
