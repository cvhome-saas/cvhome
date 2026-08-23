package com.asrevo.cvhome.content.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.asrevo.cvhome.content.entity.MediaUsageRow;

public interface MediaUsageRepository extends JpaRepository<MediaUsageRow, Long> {

    List<MediaUsageRow> findByAssetId(Long assetId);

    long countByAssetId(Long assetId);

    void deleteByContentId(Long contentId);

    void deleteByAssetId(Long assetId);

    @Query("select u.assetId, count(u) from MediaUsageRow u where u.assetId in :ids group by u.assetId")
    List<Object[]> countByAssetIds(@Param("ids") List<Long> ids);

}
