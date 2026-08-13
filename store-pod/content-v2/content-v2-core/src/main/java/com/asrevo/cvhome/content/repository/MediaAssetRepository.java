package com.asrevo.cvhome.content.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.entity.media.MediaAsset;

public interface MediaAssetRepository extends JpaRepository<MediaAsset, Long> {
    Optional<MediaAsset> findByStoreMerchantIdAndChecksumAndDeletedAtIsNull(StoreMerchantId store, String checksum);

    Optional<MediaAsset> findByIdAndStoreMerchantIdAndDeletedAtIsNull(Long id, StoreMerchantId store);

    List<MediaAsset> findAllByStoreMerchantIdAndDeletedAtIsNullOrderByAuditSectionDateCreatedDesc(
            StoreMerchantId store);

    @Query("""
            select coalesce(sum(a.byteSize), 0) from MediaAsset a
            where a.storeMerchantId = :store and a.deletedAt is null
            """)
    long sumBytesByStore(StoreMerchantId store);
}
