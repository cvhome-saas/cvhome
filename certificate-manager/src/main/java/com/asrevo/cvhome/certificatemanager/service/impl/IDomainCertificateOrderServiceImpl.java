package com.asrevo.cvhome.certificatemanager.service.impl;

import com.asrevo.cvhome.certificatemanager.domain.DomainCertificateOrder;
import com.asrevo.cvhome.certificatemanager.repository.IDomainCertificateOrderRepository;
import com.asrevo.cvhome.certificatemanager.service.IDomainCertificateOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.StreamSupport;

@Service
public class IDomainCertificateOrderServiceImpl implements IDomainCertificateOrderService {
    @Autowired
    private IDomainCertificateOrderRepository iDomainCertificateOrderRepository;

    @Override
    public DomainCertificateOrder findOneByLocation(String location) {
        return iDomainCertificateOrderRepository.findOneByLocation(location);
    }

    @Override
    public List<DomainCertificateOrder> findByDomain(String domain) {
        return iDomainCertificateOrderRepository.findByDomain(domain);
    }

    @Transactional
    @Override
    public DomainCertificateOrder save(DomainCertificateOrder domainCertificateOrder) {
        return iDomainCertificateOrderRepository.save(domainCertificateOrder);
    }

    @Override
    public List<DomainCertificateOrder> findAll() {
        return StreamSupport.stream(iDomainCertificateOrderRepository.findAll().spliterator(), false).toList();
    }

    @Override
    public List<DomainCertificateOrder> findAllOrderByIdIn(List<Long> orderIds) {
        return StreamSupport.stream(iDomainCertificateOrderRepository.findAllById(orderIds).spliterator(), false).toList();
    }
}
