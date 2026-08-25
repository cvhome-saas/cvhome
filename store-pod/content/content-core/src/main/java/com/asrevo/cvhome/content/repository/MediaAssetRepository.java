package com.asrevo.cvhome.content.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.asrevo.cvhome.content.entity.MediaAsset;

public interface MediaAssetRepository extends JpaRepository<MediaAsset, Long>, JpaSpecificationExecutor<MediaAsset> {

    Optional<MediaAsset> findByIdAndStoreMerchantId(Long id, String store);

    Optional<MediaAsset> findByStoreMerchantIdAndChecksum(String store, String checksum);

    long countByStoreMerchantIdAndFolderId(String store, Long folderId);

    List<MediaAsset> findByStoreMerchantIdAndIdIn(String store, List<Long> ids);

    @Modifying
    @Query("update MediaAsset a set a.folderId = :to where a.storeMerchantId = :store and a.folderId = :from")
    int moveFolder(@Param("store") String store, @Param("from") Long from, @Param("to") Long to);

}
