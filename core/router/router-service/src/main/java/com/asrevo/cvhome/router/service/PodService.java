package com.asrevo.cvhome.router.service;

import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.commons.domain.Reference;
import com.asrevo.cvhome.router.commons.domain.Country;
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

    PodDto getAllocation(Domain domain);

    Boolean enableReference(Reference reference);

    Boolean disableReference(Reference reference);
}
