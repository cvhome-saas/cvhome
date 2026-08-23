package com.asrevo.cvhome.content.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.asrevo.cvhome.content.entity.FaqGroup;

public interface FaqGroupRepository extends JpaRepository<FaqGroup, Long> {

    List<FaqGroup> findByStoreMerchantIdOrderByPositionAscIdAsc(String store);

    Optional<FaqGroup> findByIdAndStoreMerchantId(Long id, String store);

    Optional<FaqGroup> findByStoreMerchantIdAndKey(String store, String key);

}
