package com.aethertrack.config;

/**
 * Central registry of Kafka topic names.
 * Services consume these constants rather than hard-coding strings.
 */
public final class KafkaTopics {

    private KafkaTopics() {}

    public static final String REGIMEN_CREATED     = "aethertrack.regimen.created";
    public static final String REGIMEN_UPDATED     = "aethertrack.regimen.updated";
    public static final String OPTIMIZATION_DONE   = "aethertrack.optimization.completed";
    public static final String INTAKE_LOGGED       = "aethertrack.intake.logged";
    public static final String ADHERENCE_EVALUATED = "aethertrack.adherence.evaluated";
    public static final String FHIR_SYNC_DONE      = "aethertrack.fhir.sync.completed";
}
