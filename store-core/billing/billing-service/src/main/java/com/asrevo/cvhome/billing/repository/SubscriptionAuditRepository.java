package com.asrevo.cvhome.billing.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.asrevo.cvhome.billing.domain.SubscriptionAuditEntity;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;

public interface SubscriptionAuditRepository extends CrudRepository<SubscriptionAuditEntity, Long> {

    List<SubscriptionAuditEntity> findAllByStoreIdOrderByOccurredAtDesc(ManagerStoreId storeId);

}
