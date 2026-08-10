package com.asrevo.cvhome.billing.repository;

import org.springframework.data.repository.CrudRepository;

import com.asrevo.cvhome.billing.domain.StripeRequestEntity;

public interface StripeRequestRepository extends CrudRepository<StripeRequestEntity, String> {

}
