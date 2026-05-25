package com.aethertrack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class SupplementDomainServiceApplication {

    private static final Logger log =
            LoggerFactory.getLogger(SupplementDomainServiceApplication.class);

    private final Environment env;

    public SupplementDomainServiceApplication(Environment env) {
        this.env = env;
    }

    public static void main(String[] args) {
        SpringApplication.run(SupplementDomainServiceApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        log.info("=== AetherTrack :: supplement-domain-service READY ===");
        log.info("Active profiles : {}", (Object) env.getActiveProfiles());
        log.info("Server port     : {}", env.getProperty("server.port"));
        log.info("Custom health   : http://localhost:{}/api/health",
                env.getProperty("server.port"));
        log.info("Actuator health : http://localhost:{}/actuator/health",
                env.getProperty("server.port"));
    }
}
