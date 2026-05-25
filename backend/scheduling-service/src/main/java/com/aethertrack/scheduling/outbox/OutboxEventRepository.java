package com.aethertrack.scheduling.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    @Query("SELECT e FROM OutboxEvent e WHERE e.status = 'PENDING' ORDER BY e.createdAt ASC")
    List<OutboxEvent> findPendingEvents();

    @Modifying
    @Query("UPDATE OutboxEvent e SET e.status = :status, e.sentAt = CURRENT_TIMESTAMP WHERE e.id = :id")
    void markAs(UUID id, String status);
}
