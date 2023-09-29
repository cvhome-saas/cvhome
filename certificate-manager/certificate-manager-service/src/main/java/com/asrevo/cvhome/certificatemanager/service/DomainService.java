package com.asrevo.cvhome.certificatemanager.service;

import com.asrevo.cvhome.certificatemanager.commons.domain.CertificateId;
import com.asrevo.cvhome.certificatemanager.commons.domain.Domain;
import com.asrevo.cvhome.certificatemanager.commons.domain.OrdersId;
import com.asrevo.cvhome.certificatemanager.commons.dto.DomainCreateRequestDto;
import com.asrevo.cvhome.certificatemanager.commons.dto.DomainCreateResponseDto;
import com.asrevo.cvhome.certificatemanager.entity.DomainEntity;

public interface DomainService {
    DomainCreateResponseDto register(DomainCreateRequestDto createRequest);

    DomainCreateResponseDto register(Domain domain, boolean autoRenew, boolean autoOrder);

    DomainEntity save(DomainEntity entity);

    DomainEntity findOneByDomain(Domain domain);

    void updateDomainStatus(Domain domain);

    void addNewCertificateToDomain(Domain domain, OrdersId ordersId, CertificateId certificateId);

}
