package com.asrevo.cvhome.content.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.entity.page.ContentPage;

public interface ContentPageRepository extends JpaRepository<ContentPage, Long> {
    Optional<ContentPage> findByIdAndContentStoreMerchantId(Long id, StoreMerchantId store);
}
