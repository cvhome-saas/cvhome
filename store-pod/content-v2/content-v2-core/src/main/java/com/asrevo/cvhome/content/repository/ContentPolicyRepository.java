package com.asrevo.cvhome.content.repository;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.entity.policy.ContentPolicy;
import com.asrevo.cvhome.content.model.policy.PolicyType;

public interface ContentPolicyRepository extends JpaRepository<ContentPolicy, Long> {
    Optional<ContentPolicy> findByIdAndStoreMerchantId(Long id, StoreMerchantId store);

    List<ContentPolicy> findAllByStoreMerchantIdAndPolicyTypeOrderByEffectiveDateDesc(
            StoreMerchantId store, PolicyType policyType);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<ContentPolicy> findForUpdateByStoreMerchantIdAndPolicyTypeOrderByEffectiveDateDesc(
            StoreMerchantId store, PolicyType policyType);
}
