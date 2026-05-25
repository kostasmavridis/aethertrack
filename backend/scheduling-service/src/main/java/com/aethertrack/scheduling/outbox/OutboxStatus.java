package com.aethertrack.scheduling.outbox;

public final class OutboxStatus {
    public static final String PENDING = "PENDING";
    public static final String SENT    = "SENT";
    public static final String FAILED  = "FAILED";
    private OutboxStatus() {}
}
