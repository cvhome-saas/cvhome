package com.asrevo.cvhome.billing.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.asrevo.cvhome.billing.commons.PlanId;
import com.asrevo.cvhome.billing.domain.PlanEntity;

public interface PlanRepository extends CrudRepository<PlanEntity, PlanId> {

    Optional<PlanEntity> findByCode(String code);

    List<PlanEntity> findAllByActiveTrueOrderByTierAsc();

}
