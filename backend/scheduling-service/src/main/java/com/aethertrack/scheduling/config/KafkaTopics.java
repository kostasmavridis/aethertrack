package com.aethertrack.scheduling.config;

public final class KafkaTopics {
    private KafkaTopics() {}

    public static final String REGIMEN_CREATED   = "aethertrack.regimen.created";
    public static final String OPTIMIZATION_DONE = "aethertrack.optimization.completed";
}
