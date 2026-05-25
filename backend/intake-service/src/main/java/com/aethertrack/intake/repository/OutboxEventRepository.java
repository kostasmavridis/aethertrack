package com.aethertrack.intake.repository;

import com.aethertrack.intake.domain.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {
    List<OutboxEvent> findTop20ByStatusOrderByCreatedAtAsc(String status);
}
