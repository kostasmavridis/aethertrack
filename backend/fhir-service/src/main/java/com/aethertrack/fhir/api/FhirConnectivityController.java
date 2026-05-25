package com.aethertrack.fhir.api;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.aethertrack.fhir.config.FhirProperties;
import org.hl7.fhir.r5.model.CapabilityStatement;
import org.hl7.fhir.r5.model.Patient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * REST endpoints for manual FHIR connectivity verification.
 *
 * <ul>
 *   <li>{@code GET /api/fhir/connectivity} – fetches CapabilityStatement,
 *       returns {status, fhirVersion, publisher, baseUrl, checkedAt}</li>
 *   <li>{@code GET /api/fhir/patient/{id}} – demo Patient read
 *       (used for smoke testing; returns 404 if not found)</li>
 * </ul>
 *
 * These endpoints are NOT secured in Slice 10; OAuth2 is added in Slice 20.
 */
@RestController
@RequestMapping("/api/fhir")
public class FhirConnectivityController {

    private final IGenericClient fhirClient;
    private final FhirProperties props;

    public FhirConnectivityController(IGenericClient fhirClient, FhirProperties props) {
        this.fhirClient = fhirClient;
        this.props      = props;
    }

    /** Fetches CapabilityStatement and returns key metadata as JSON. */
    @GetMapping("/connectivity")
    public ResponseEntity<Map<String, Object>> checkConnectivity() {
        try {
            CapabilityStatement cs = fhirClient
                .capabilities()
                .ofType(CapabilityStatement.class)
                .execute();

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("status",      "UP");
            body.put("fhirVersion", cs.getFhirVersion() != null
                ? cs.getFhirVersion().toCode() : "unknown");
            body.put("publisher",   cs.getSoftware() != null
                ? cs.getSoftware().getName() : "unknown");
            body.put("baseUrl",     props.baseUrl());
            body.put("checkedAt",   Instant.now().toString());
            return ResponseEntity.ok(body);

        } catch (Exception ex) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("status",  "DOWN");
            body.put("baseUrl", props.baseUrl());
            body.put("error",   ex.getMessage());
            return ResponseEntity.status(503).body(body);
        }
    }

    /**
     * Demo endpoint: read a Patient resource by FHIR logical ID.
     * Returns 200 + Patient JSON, or 404 if not found on the HAPI server.
     */
    @GetMapping("/patient/{id}")
    public ResponseEntity<String> readPatient(@PathVariable String id) {
        try {
            Patient patient = fhirClient
                .read()
                .resource(Patient.class)
                .withId(id)
                .execute();

            // Encode to JSON string using HAPI's own encoder for valid FHIR JSON
            String json = fhirClient.getFhirContext()
                .newJsonParser()
                .setPrettyPrint(true)
                .encodeResourceToString(patient);

            return ResponseEntity.ok()
                .header("Content-Type", "application/fhir+json")
                .body(json);

        } catch (ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException ex) {
            return ResponseEntity.notFound().build();
        } catch (Exception ex) {
            return ResponseEntity.status(503).body("{\"error\":\"" + ex.getMessage() + "\"}");
        }
    }
}
