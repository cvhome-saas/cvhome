package com.asrevo.cvhome.router.service;

import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.commons.domain.DomainReference;
import com.asrevo.cvhome.commons.domain.Country;
import com.asrevo.cvhome.commons.dto.PodDto;
import com.asrevo.cvhome.commons.dto.PodReferenceDto;
import com.asrevo.cvhome.router.commons.dto.*;
import com.asrevo.cvhome.router.entity.PodEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PodService {
    PodDto create(CreatePodDto dto);

    Page<PodEntity> findAll(PodDto podDto, Pageable pageable);

    PodDto selectPod(Domain domain, Country country);

    CreateReferenceResponse createReference(CreateNewReferenceDto createReferenceDto);

    CreateReferenceResponse addAlis(AddAlisDto addAlisDto);

    PodReferenceDto getAllocation(Domain domain);

    Boolean enableReference(DomainReference reference);

    Boolean disableReference(DomainReference reference);
}
