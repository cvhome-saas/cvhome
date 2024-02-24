package com.asrevo.cvhome.domaincertificatemanager.repository;

import com.asrevo.cvhome.domaincertificatemanager.commons.domain.OrdersId;
import com.asrevo.cvhome.domaincertificatemanager.entity.OrdersEntity;
import org.springframework.data.repository.ListCrudRepository;

public interface OrdersRepository extends ListCrudRepository<OrdersEntity, OrdersId> {
}
