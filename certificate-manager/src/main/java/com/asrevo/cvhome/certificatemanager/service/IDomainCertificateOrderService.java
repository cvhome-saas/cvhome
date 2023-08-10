package com.asrevo.cvhome.certificatemanager.service;

import com.asrevo.cvhome.certificatemanager.domain.DomainCertificateOrder;

import java.util.List;

public interface IDomainCertificateOrderService {
    DomainCertificateOrder findOneByLocation(String location);

    List<DomainCertificateOrder> findByDomain(String domain);

    DomainCertificateOrder save(DomainCertificateOrder domainCertificateOrder);

    List<DomainCertificateOrder> findAll();

}
