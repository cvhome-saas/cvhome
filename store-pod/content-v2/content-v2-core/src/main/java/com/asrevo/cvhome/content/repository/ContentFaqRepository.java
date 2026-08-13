package com.asrevo.cvhome.content.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.entity.faq.ContentFaq;

public interface ContentFaqRepository extends JpaRepository<ContentFaq, Long> {
    Optional<ContentFaq> findByIdAndContentStoreMerchantId(Long id, StoreMerchantId store);

    List<ContentFaq> findAllByGroupIdAndContentStoreMerchantIdOrderByPositionAscIdAsc(
            Long groupId, StoreMerchantId store);
}
