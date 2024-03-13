package com.asrevo.cvhome.domaincertificatemanager.service.impl;

import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.domaincertificatemanager.commons.domain.Domain;
import com.asrevo.cvhome.domaincertificatemanager.commons.domain.DomainType;
import com.asrevo.cvhome.domaincertificatemanager.commons.domain.PodId;
import com.asrevo.cvhome.domaincertificatemanager.commons.domain.Reference;
import com.asrevo.cvhome.domaincertificatemanager.commons.dto.PodDto;
import com.asrevo.cvhome.domaincertificatemanager.mappers.PodMappers;
import com.asrevo.cvhome.domaincertificatemanager.repository.PodRepository;
import com.asrevo.cvhome.domaincertificatemanager.service.PodService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PodServiceImpl implements PodService {
    private final PodRepository podRepository;
    private final PodMappers podMappers;

    @Override
    public PodId selectPod(Domain domain, Reference reference, DomainType domainType, IdentityId identity) {
        return new PodId("65f023632bc46470c204b78f");
    }

    @Override
    public PodDto getPod(PodId podId) {
        return podRepository.findById(podId).map(podMappers::toPodDto).orElse(null);
    }
}
