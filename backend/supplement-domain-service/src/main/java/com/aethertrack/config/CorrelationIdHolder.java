package com.aethertrack.config;

import java.util.UUID;

/**
 * Thread-local holder for a per-request correlation ID.
 * Populated by CorrelationIdFilter; consumed by event publishers.
 */
public final class CorrelationIdHolder {

    private static final ThreadLocal<String> HOLDER = new ThreadLocal<>();

    private CorrelationIdHolder() {}

    public static void set(String id) {
        HOLDER.set(id);
    }

    public static String get() {
        String id = HOLDER.get();
        return (id != null) ? id : UUID.randomUUID().toString();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
