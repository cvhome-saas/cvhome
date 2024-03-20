package com.asrevo.cvhome.router.service;


import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.commons.domain.Reference;
import com.asrevo.cvhome.commons.domain.ReferenceType;
import com.asrevo.cvhome.commons.utils.OperationExecution;
import com.asrevo.cvhome.router.commons.domain.Country;
import com.asrevo.cvhome.router.commons.domain.PodRegion;
import com.asrevo.cvhome.router.commons.domain.PodSubRegion;
import com.asrevo.cvhome.router.commons.domain.PodType;
import com.asrevo.cvhome.router.commons.dto.*;
import com.asrevo.cvhome.router.entity.PodEntity;
import com.asrevo.cvhome.router.mappers.PodMappersImpl;
import com.asrevo.cvhome.router.mappers.ReferenceMappersImpl;
import com.asrevo.cvhome.router.service.impl.PodServiceImpl;
import org.junit.Assert;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JacksonAutoConfiguration.class, PodMappersImpl.class, PodServiceImpl.class, ReferenceMappersImpl.class})
@Testcontainers
@Tag("integration-test")
class PodServiceTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13");
    @Autowired
    private PodService podService;
    private final Reference reference = new Reference("1231231324456", ReferenceType.STORE);
    private final CreateNewReferenceDto createReferenceDto = new CreateNewReferenceDto(reference, new Domain("ass.com"), Country.EG);

    @Test
    void shouldCreatePod() {
        PodDto podDto = podService.create(new CreatePodDto(PodRegion.EU_CENTRAL_1, PodSubRegion.R1, PodType.EXTERNAL, "namespace", "location", "locationAlis"));
        assertThat(podDto, notNullValue());
        assertThat(podDto.id(), notNullValue());
    }

    @Test
    void findAll() {
        podService.create(new CreatePodDto(PodRegion.EU_CENTRAL_2, PodSubRegion.R1, PodType.EXTERNAL, "namespace", "location", "locationAlis"));
        Page<PodEntity> all = podService.findAll(new PodDto(null, PodRegion.EU_CENTRAL_2, PodSubRegion.R1, null, null, null, null), PageRequest.of(0, 5));
        assertThat(all, notNullValue());
        assertThat(all.getSize(), greaterThan(1));
    }

    @Test
    void selectPodShouldNotReturnNull() {
        PodDto podDto = podService.selectPod(new Domain("domain.com"), Country.EG);
        assertThat(podDto, notNullValue());
    }

    @Test
    void shouldCreateReference() {
        CreateReferenceResponse referenceResponse = podService.createReference(createReferenceDto);
        assertThat(referenceResponse, notNullValue());
    }

    @Test
    void shouldAddAlis() {
        podService.createReference(createReferenceDto);
        CreateReferenceResponse referenceResponse = podService.addAlis(new AddAlisDto(reference, new Domain("extra.com")));
        assertThat(referenceResponse, notNullValue());
        assertThat(referenceResponse.referenceDto(), notNullValue());
        assertThat(referenceResponse.referenceDto().alis(), hasSize(2));
    }

    @Test
    void getAllocation() {
        podService.createReference(createReferenceDto);
        PodDto allocation = podService.getAllocation(createReferenceDto.domain());
        assertThat(allocation, notNullValue());
        assertThat(allocation.region(), notNullValue());
        assertThat(allocation.subRegion(), notNullValue());
    }

    @Test
    void enableReference() {
        podService.createReference(createReferenceDto);
        podService.enableReference(createReferenceDto.reference());
        PodDto allocation = podService.getAllocation(createReferenceDto.domain());
        assertThat(allocation, notNullValue());
        assertThat(allocation.region(), notNullValue());
        assertThat(allocation.subRegion(), notNullValue());
    }

    @Test
    void disableReference() {
        podService.createReference(createReferenceDto);
        podService.disableReference(createReferenceDto.reference());
        Assert.assertThrows("alis not exist", OperationExecution.class, () -> podService.getAllocation(createReferenceDto.domain()));
    }

}