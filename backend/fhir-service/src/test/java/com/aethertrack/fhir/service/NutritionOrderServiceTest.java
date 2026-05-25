package com.aethertrack.fhir.service;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.ICreate;
import ca.uhn.fhir.rest.gclient.ICreateTyped;
import com.aethertrack.fhir.config.FhirProperties;
import com.aethertrack.fhir.domain.RegimenFhirMapping;
import com.aethertrack.fhir.events.RegimenCreatedPayload;
import com.aethertrack.fhir.events.RegimenCreatedPayload.RegimenItem;
import com.aethertrack.fhir.mapper.NutritionOrderMapper;
import com.aethertrack.fhir.repository.RegimenFhirMappingRepository;
import org.hl7.fhir.r5.model.IdType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NutritionOrderServiceTest {

    @Mock NutritionOrderMapper         mapper;
    @Mock IGenericClient               fhirClient;
    @Mock FhirProperties               props;
    @Mock RegimenFhirMappingRepository mappingRepo;
    @Mock ICreate                      createStep;
    @Mock ICreateTyped                 createTypedStep;

    NutritionOrderService service;

    @BeforeEach void setup() {
        service = new NutritionOrderService(mapper, fhirClient, props, mappingRepo);
    }

    private RegimenCreatedPayload payload() {
        return new RegimenCreatedPayload(42L, "p-001", "Test",
            List.of(new RegimenItem(1L, 10L, "VIT-D3", BigDecimal.ONE, "IU", 1, null)));
    }

    @Test void createFromRegimen_createsAndPersistsMapping() throws Exception {
        when(mappingRepo.existsByRegimenId(42L)).thenReturn(false);
        var no = new org.hl7.fhir.r5.model.NutritionOrder();
        when(mapper.toNutritionOrder(any())).thenReturn(no);
        when(fhirClient.create()).thenReturn(createStep);
        when(createStep.resource(any())).thenReturn(createTypedStep);
        MethodOutcome outcome = new MethodOutcome();
        outcome.setId(new IdType("NutritionOrder", "99"));
        when(createTypedStep.execute()).thenReturn(outcome);
        when(props.baseUrl()).thenReturn("http://hapi/fhir");

        service.createFromRegimen(payload(), "corr-1");

        ArgumentCaptor<RegimenFhirMapping> captor = ArgumentCaptor.forClass(RegimenFhirMapping.class);
        verify(mappingRepo).save(captor.capture());
        assertThat(captor.getValue().getNutritionOrderId()).isEqualTo("99");
        assertThat(captor.getValue().getRegimenId()).isEqualTo(42L);
    }

    @Test void createFromRegimen_skipsWhenMappingExists() {
        when(mappingRepo.existsByRegimenId(42L)).thenReturn(true);
        service.createFromRegimen(payload(), "corr-1");
        verifyNoInteractions(fhirClient);
        verify(mappingRepo, never()).save(any());
    }

    @Test void createFromRegimen_propagatesHapiException() {
        when(mappingRepo.existsByRegimenId(42L)).thenReturn(false);
        when(mapper.toNutritionOrder(any())).thenReturn(new org.hl7.fhir.r5.model.NutritionOrder());
        when(fhirClient.create()).thenThrow(new RuntimeException("HAPI down"));
        assertThatThrownBy(() -> service.createFromRegimen(payload(), null))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("HAPI down");
    }
}
