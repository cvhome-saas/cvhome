package com.asrevo.cvhome.certificatemanager.repository;

import com.asrevo.cvhome.commons.domain.CertificateOrderStatus;
import com.asrevo.cvhome.commons.domain.DomainCertificateOrder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.time.Instant;
import java.util.List;
import java.util.Set;

public interface DomainCertificateOrderRepository extends CrudRepository<DomainCertificateOrder, Long> {
    List<DomainCertificateOrder> findByCertificateOrderStatusInAndValidatedDateLessThanOrderByIdAsc(Set<CertificateOrderStatus> statuses, Instant from, Pageable pageable);

    List<DomainCertificateOrder> findByCertificateOrderStatusInAndCreatedDateLessThanOrderByIdAsc(Set<CertificateOrderStatus> statuses, Instant from, Pageable pageable);
}
