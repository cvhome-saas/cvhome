package com.asrevo.cvhome.certificatemanager.service.impl;

import com.asrevo.cvhome.certificatemanager.repository.DomainCertificateOrderRepository;
import com.asrevo.cvhome.certificatemanager.service.DomainCertificateOrderService;
import com.asrevo.cvhome.commons.domain.CertificateOrderStatus;
import com.asrevo.cvhome.commons.domain.DomainCertificateOrder;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.StreamSupport;

@Service
@AllArgsConstructor
public class DomainCertificateOrderServiceImpl implements DomainCertificateOrderService {
    private final DomainCertificateOrderRepository domainCertificateOrderRepository;

    @Override
    public Optional<DomainCertificateOrder> findOneById(Long id) {
        return domainCertificateOrderRepository.findById(id);
    }

    @Transactional
    @Override
    public DomainCertificateOrder save(DomainCertificateOrder domainCertificateOrder) {
        return domainCertificateOrderRepository.save(domainCertificateOrder);
    }

    @Override
    public List<DomainCertificateOrder> findAllOrderByIdIn(List<Long> orderIds) {
        return StreamSupport.stream(domainCertificateOrderRepository.findAllById(orderIds).spliterator(), false).toList();
    }

    @Override
    public List<DomainCertificateOrder> findAllSinceValidation(Set<CertificateOrderStatus> statuses, Instant from, int limit) {
        return domainCertificateOrderRepository.findByCertificateOrderStatusInAndValidatedDateLessThanOrderByIdAsc(statuses, from, PageRequest.of(0, limit));
    }

    @Override
    public List<DomainCertificateOrder> findAllSinceCreation(Set<CertificateOrderStatus> statuses, Instant from, int limit) {
        return domainCertificateOrderRepository.findByCertificateOrderStatusInAndCreatedDateLessThanOrderByIdAsc(statuses, from, PageRequest.of(0, limit));
    }

}
