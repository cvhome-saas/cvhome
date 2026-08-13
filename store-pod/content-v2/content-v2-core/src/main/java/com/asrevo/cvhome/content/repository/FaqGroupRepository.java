package com.asrevo.cvhome.content.repository;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.entity.faq.FaqGroup;

public interface FaqGroupRepository extends JpaRepository<FaqGroup, Long> {
    Optional<FaqGroup> findByIdAndStoreMerchantId(Long id, StoreMerchantId store);

    List<FaqGroup> findAllByStoreMerchantIdOrderByPositionAscIdAsc(StoreMerchantId store);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<FaqGroup> findForUpdateByIdAndStoreMerchantId(Long id, StoreMerchantId store);
}
