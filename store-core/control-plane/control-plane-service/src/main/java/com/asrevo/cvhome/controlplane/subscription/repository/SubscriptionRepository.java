package com.asrevo.cvhome.controlplane.subscription.repository;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.subscription.commons.SubscriptionStatus;
import com.asrevo.cvhome.controlplane.subscription.domain.SubscriptionEntity;
import java.time.Instant;
import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface SubscriptionRepository extends CrudRepository<SubscriptionEntity, ManagerOrgId> {

	List<SubscriptionEntity> findAllByStatusAndEndDateBefore(SubscriptionStatus status, Instant endDateBefore);

}
