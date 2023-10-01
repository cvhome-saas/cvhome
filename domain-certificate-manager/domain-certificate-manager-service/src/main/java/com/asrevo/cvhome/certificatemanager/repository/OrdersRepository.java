package com.asrevo.cvhome.certificatemanager.repository;

import com.asrevo.cvhome.certificatemanager.commons.domain.OrdersId;
import com.asrevo.cvhome.certificatemanager.entity.OrdersEntity;
import org.springframework.data.repository.ListCrudRepository;

public interface OrdersRepository extends ListCrudRepository<OrdersEntity, OrdersId> {
}
