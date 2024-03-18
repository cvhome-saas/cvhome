package com.asrevo.cvhome.dcm.repository;

import com.asrevo.cvhome.dcm.commons.domain.OrdersId;
import com.asrevo.cvhome.dcm.entity.OrdersEntity;
import org.springframework.data.repository.ListCrudRepository;

public interface OrdersRepository extends ListCrudRepository<OrdersEntity, OrdersId> {
}
