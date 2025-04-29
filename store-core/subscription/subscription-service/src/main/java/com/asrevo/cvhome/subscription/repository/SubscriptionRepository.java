package com.asrevo.cvhome.subscription.repository;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.subscription.commons.SubscriptionStatus;
import com.asrevo.cvhome.subscription.domain.SubscriptionEntity;
import org.springframework.data.repository.CrudRepository;

import java.time.Instant;
import java.util.List;

public interface SubscriptionRepository extends CrudRepository<SubscriptionEntity, ManagerOrgId> {

    List<SubscriptionEntity> findAllByStatusAndEndDateBefore(SubscriptionStatus status, Instant endDateBefore);
}
