package com.asrevo.cvhome.content.repository;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.entity.banner.ContentBanner;
import com.asrevo.cvhome.content.model.banner.BannerPlacement;

public interface ContentBannerRepository extends JpaRepository<ContentBanner, Long> {
    Optional<ContentBanner> findByIdAndContentStoreMerchantId(Long id, StoreMerchantId store);

    List<ContentBanner> findAllByContentStoreMerchantIdAndPlacementOrderByPositionAscIdAsc(
            StoreMerchantId store, BannerPlacement placement);

    long countByContentStoreMerchantIdAndPlacement(StoreMerchantId store, BannerPlacement placement);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<ContentBanner> findForUpdateByContentStoreMerchantIdAndPlacementOrderByPositionAscIdAsc(
            StoreMerchantId store, BannerPlacement placement);
}
