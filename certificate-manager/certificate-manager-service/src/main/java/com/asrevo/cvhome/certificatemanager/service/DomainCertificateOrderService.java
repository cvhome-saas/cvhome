package com.asrevo.cvhome.certificatemanager.service;

import com.asrevo.cvhome.certificatemanager.domain.DomainCertificateOrder;
import com.asrevo.cvhome.commons.domain.CertificateOrderStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface DomainCertificateOrderService {

    Optional<DomainCertificateOrder> findOneById(Long id);

    DomainCertificateOrder save(DomainCertificateOrder domainCertificateOrder);

    List<DomainCertificateOrder> findAllOrderByIdIn(List<Long> orderIds);

    List<DomainCertificateOrder> findAllSinceValidation(Set<CertificateOrderStatus> statuses, Instant from, int limit);

    List<DomainCertificateOrder> findAllSinceCreation(Set<CertificateOrderStatus> statuses, Instant from, int limit);
}
