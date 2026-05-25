package com.aethertrack.scheduling.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Reads regimen items from the scheduling read schema view.
 * Uses Spring's {@link JdbcClient} (Spring 6.1+) for clean, type-safe queries.
 */
@Repository
public class RegimenItemRepository {

    private final JdbcClient jdbcClient;

    public RegimenItemRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<RegimenItemReadModel> findByRegimenId(Long regimenId) {
        return jdbcClient.sql("""
                SELECT item_id,
                       regimen_id,
                       supplement_id,
                       supplement_code,
                       supplement_category,
                       dose_qty,
                       dose_unit,
                       frequency_per_day,
                       schedule_window,
                       night_time_required,
                       meal_required
                FROM   scheduling.v_regimen_item
                WHERE  regimen_id = :regimenId
                ORDER BY item_id
                """)
            .param("regimenId", regimenId)
            .query(RegimenItemReadModel.class)
            .list();
    }
}
