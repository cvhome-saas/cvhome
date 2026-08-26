package com.asrevo.cvhome.content.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.asrevo.cvhome.content.entity.MediaUsageRow;
import com.asrevo.cvhome.content.model.MediaOwnerKind;

public interface MediaUsageRepository extends JpaRepository<MediaUsageRow, Long> {

    List<MediaUsageRow> findByAssetId(Long assetId);

    long countByAssetId(Long assetId);

    /**
     * A bulk delete, not a derived one: Hibernate flushes inserts before deletes, so re-stating an owner's
     * references would insert a duplicate of a row it was about to remove and trip the unique constraint.
     * Executing the delete as its own statement puts it ahead of the inserts.
     *
     * <p>
     * Deliberately not {@code clearAutomatically}: this runs inside the caller's transaction, and clearing the
     * persistence context detaches the content item it is part-way through saving.
     * </p>
     */
    @Modifying(flushAutomatically = true)
    @Query("delete from MediaUsageRow u where u.ownerKind = :kind and u.ownerRef = :ref")
    void deleteByOwner(@Param("kind") MediaOwnerKind kind, @Param("ref") String ref);

    void deleteByAssetId(Long assetId);

    @Query("select u.assetId, count(u) from MediaUsageRow u where u.assetId in :ids group by u.assetId")
    List<Object[]> countByAssetIds(@Param("ids") List<Long> ids);

}
