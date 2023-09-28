package com.asrevo.cvhome.certificatemanager.repository;

import com.asrevo.cvhome.certificatemanager.commons.domain.CertificateOrderStatus;
import com.asrevo.cvhome.certificatemanager.commons.domain.OrdersId;
import com.asrevo.cvhome.certificatemanager.entity.OrdersEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.ListCrudRepository;

import java.time.Instant;
import java.util.List;
import java.util.Set;

public interface OrdersRepository extends ListCrudRepository<OrdersEntity, OrdersId> {
    List<OrdersEntity> findByCertificateOrderStatusInAndValidatedDateLessThanOrderByIdAsc(Set<CertificateOrderStatus> statuses, Instant from, PageRequest request);

    List<OrdersEntity> findByCertificateOrderStatusInAndCreatedDateLessThanOrderByIdAsc(Set<CertificateOrderStatus> statuses, Instant from, PageRequest request);
}
