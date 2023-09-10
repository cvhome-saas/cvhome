package com.asrevo.cvhome.certificatemanager.repository;

import com.asrevo.cvhome.commons.domain.CertificateOrderStatus;
import com.asrevo.cvhome.commons.domain.DomainCertificateOrder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface DomainCertificateOrderRepository extends CrudRepository<DomainCertificateOrder, Long> {
    List<DomainCertificateOrder> findByCertificateOrderStatusOrderByIdAsc(CertificateOrderStatus status, Pageable pageable);

    DomainCertificateOrder findOneByIdOrLocation(Long id, String location);

    Optional<DomainCertificateOrder> findOneByIdAndCertificateOrderStatusIn(Long orderId, Set<CertificateOrderStatus> statuses);
}
