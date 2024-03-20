package com.asrevo.cvhome.router.service;

import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.commons.domain.Reference;
import com.asrevo.cvhome.router.commons.domain.Country;
import com.asrevo.cvhome.router.commons.dto.AddAlisDto;
import com.asrevo.cvhome.router.commons.dto.CreateNewReferenceDto;
import com.asrevo.cvhome.router.commons.dto.CreateReferenceResponse;
import com.asrevo.cvhome.router.commons.dto.PodDto;

public interface PodService {

    PodDto selectPod(Domain domain, Country country);

    CreateReferenceResponse createReference(CreateNewReferenceDto createReferenceDto);

    CreateReferenceResponse addAlis(AddAlisDto addAlisDto);

    PodDto getAllocation(Domain domain);

    Boolean enableReference(Reference reference);

    Boolean disableReference(Reference reference);
}
