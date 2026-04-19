package com.asrevo.cvhome.controlplane.subscription.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.controlplane.subscription.commons.SubscriptionStatus;
import com.asrevo.cvhome.controlplane.subscription.domain.SubscriptionEntity;

public interface SubscriptionRepository extends CrudRepository<SubscriptionEntity, ManagerOrgId> {

    List<SubscriptionEntity> findAllByStatusAndEndDateBefore(SubscriptionStatus status, Instant endDateBefore);

}
