package com.asrevo.cvhome.certificatemanager.repository;

import com.asrevo.cvhome.certificatemanager.entity.OrdersEntity;
import com.asrevo.cvhome.commons.domain.OrdersId;
import org.springframework.data.repository.ListCrudRepository;

public interface OrdersRepository extends ListCrudRepository<OrdersEntity, OrdersId> {
}
