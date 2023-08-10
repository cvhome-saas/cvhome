package com.asrevo.cvhome.certificatemanager.repository;

import com.asrevo.cvhome.certificatemanager.domain.DomainCertificateOrder;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface IDomainCertificateOrderRepository extends CrudRepository<DomainCertificateOrder, Long> {
    DomainCertificateOrder findOneByLocation(String location);

    List<DomainCertificateOrder> findByDomain(String domain);
}
