package com.asrevo.cvhome.billing.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.asrevo.cvhome.billing.commons.PlanEntitlementId;
import com.asrevo.cvhome.billing.commons.PlanId;
import com.asrevo.cvhome.billing.domain.PlanEntitlementEntity;

public interface PlanEntitlementRepository extends CrudRepository<PlanEntitlementEntity, PlanEntitlementId> {

    List<PlanEntitlementEntity> findAllByPlanId(PlanId planId);

}
