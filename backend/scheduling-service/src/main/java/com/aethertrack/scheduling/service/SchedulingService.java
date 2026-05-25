package com.aethertrack.scheduling.service;

import ai.timefold.solver.core.api.solver.SolverJob;
import ai.timefold.solver.core.api.solver.SolverManager;
import com.aethertrack.scheduling.domain.SupplementDose;
import com.aethertrack.scheduling.domain.SupplementSchedule;
import com.aethertrack.scheduling.events.RegimenCreatedPayload;
import com.aethertrack.scheduling.repository.RegimenItemReadModel;
import com.aethertrack.scheduling.repository.RegimenItemRepository;
import com.aethertrack.scheduling.solver.PlanningProblemMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Orchestrates: DB fetch → Map → Solve → Persist → Outbox.
 *
 * Slice 8: DB fetch, map, solve, log.
 * Slice 9: persist + outbox via {@link SchedulePersistenceService}.
 */
@Service
public class SchedulingService {

    private static final Logger log = LoggerFactory.getLogger(SchedulingService.class);

    private final RegimenItemRepository        regimenItemRepository;
    private final PlanningProblemMapper         mapper;
    private final SolverManager<SupplementSchedule, Long> solverManager;
    private final SchedulePersistenceService    persistenceService;

    public SchedulingService(RegimenItemRepository regimenItemRepository,
                              PlanningProblemMapper mapper,
                              SolverManager<SupplementSchedule, Long> solverManager,
                              SchedulePersistenceService persistenceService) {
        this.regimenItemRepository = regimenItemRepository;
        this.mapper                = mapper;
        this.solverManager         = solverManager;
        this.persistenceService    = persistenceService;
    }

    public void scheduleRegimen(RegimenCreatedPayload payload, String correlationId) {
        Long regimenId = payload.regimenId();
        log.info("[scheduleRegimen] Starting regimenId={} patientId={}", regimenId, payload.patientId());

        SupplementSchedule problem = buildProblem(payload);

        if (problem.getDoses().isEmpty()) {
            log.warn("[scheduleRegimen] No doses for regimenId={} – skipping solver", regimenId);
            return;
        }

        log.info("[scheduleRegimen] {} doses, {} slots – submitting to solver",
                 problem.getDoses().size(), problem.getTimeSlots().size());
        solve(regimenId, problem, correlationId);
    }

    /** Backwards-compatible overload for callers that don't have a correlationId. */
    public void scheduleRegimen(RegimenCreatedPayload payload) {
        scheduleRegimen(payload, null);
    }

    private SupplementSchedule buildProblem(RegimenCreatedPayload payload) {
        try {
            List<RegimenItemReadModel> items = regimenItemRepository.findByRegimenId(payload.regimenId());
            if (items.isEmpty()) {
                log.warn("[buildProblem] DB empty for regimenId={} – falling back to payload", payload.regimenId());
                return mapper.fromEventPayload(payload);
            }
            return mapper.fromReadModels(payload.regimenId(), payload.patientId(), items);
        } catch (DataAccessException ex) {
            log.error("[buildProblem] DB error regimenId={}: {} – using payload fallback",
                      payload.regimenId(), ex.getMessage());
            return mapper.fromEventPayload(payload);
        }
    }

    private void solve(Long regimenId, SupplementSchedule problem, String correlationId) {
        SolverJob<SupplementSchedule, Long> job = solverManager.solveAndListen(
            regimenId,
            id -> problem,
            sol -> { if (sol.getScore() != null)
                log.debug("[solver] Best score regimenId={}: {}", regimenId, sol.getScore()); },
            (id, ex) -> log.error("[solver] Error regimenId={}: {}", id, ex.getMessage(), ex)
        );
        try {
            SupplementSchedule solution = job.getFinalBestSolution();
            logSchedule(solution);
            persistenceService.persistAndEnqueue(solution, correlationId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[solve] Interrupted regimenId={}", regimenId);
        } catch (ExecutionException e) {
            log.error("[solve] Execution failed regimenId={}: {}", regimenId, e.getMessage(), e);
        }
    }

    private void logSchedule(SupplementSchedule solution) {
        log.info("╔{}", "=".repeat(58));
        log.info("║  SCHEDULE regimenId={}  score={}", solution.getRegimenId(), solution.getScore());
        log.info("╠{}", "=".repeat(58));
        solution.getDoses().stream()
            .filter(d -> d.getAssignedSlot() != null)
            .sorted((a, b) -> a.getAssignedSlot().getStartTime().compareTo(b.getAssignedSlot().getStartTime()))
            .forEach(d -> log.info("║  {:>10} | {:<16} {} {}",
                d.getAssignedSlot().getId(), d.getSupplementCode(),
                d.getDoseQty() + " " + d.getDoseUnit(), buildFlags(d)));
        log.info("╚{}", "=".repeat(58));
    }

    private String buildFlags(SupplementDose d) {
        StringBuilder sb = new StringBuilder();
        if (d.isNightTimeRequired()) sb.append("[NIGHT]");
        if (d.isMealRequired())      sb.append("[MEAL]");
        return sb.toString();
    }
}
