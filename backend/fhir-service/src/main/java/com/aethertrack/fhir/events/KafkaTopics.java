package com.aethertrack.fhir.events;

public final class KafkaTopics {
    public static final String REGIMEN_CREATED = "regimen.created";
    public static final String OPTIMIZATION_COMPLETED = "optimization.completed";
    public static final String FHIR_SYNC_COMPLETED = "fhir.sync.completed";
    public static final String FHIR_SYNC_FAILED = "fhir.sync.failed";

    private KafkaTopics() {}
}
