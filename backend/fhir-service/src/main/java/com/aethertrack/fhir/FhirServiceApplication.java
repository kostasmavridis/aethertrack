package com.aethertrack.fhir;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class FhirServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(FhirServiceApplication.class, args);
    }
}
