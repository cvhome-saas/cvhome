package com.asrevo.cvhome.podregistry.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.podregistry.domain.PodEntity;

@Repository
public interface PodRepository extends CrudRepository<PodEntity, PodId>, PagingAndSortingRepository<PodEntity, PodId> {

    Page<PodEntity> findAllByOrgId(ManagerOrgId orgId, Pageable pageable);

    /** Every pod an organization owns privately — the only candidates placement may consider for it. */
    List<PodEntity> findAllByOrgId(ManagerOrgId orgId);

    Optional<PodEntity> findByName(String name);

    /**
     * The pods a store with no private pod may be placed on.
     *
     * <p>
     * A real predicate, not "everything". The registry this replaced answered its {@code listPublicPods} with
     * {@code findAll}, so an organization with no private pod of its own could be placed onto <em>another
     * organization's</em> private pod. Both halves are checked — visibility and the absence of an owner — so a pod
     * held out of rotation by flipping one of them stays out.
     * </p>
     */
    @Query("""
            select * from pod_registry.pod
            where visibility = 'PUBLIC' and org_id is null""")
    List<PodEntity> findPlaceablePublicPods();

    /**
     * Claims the right to run the start-up seed, without waiting.
     *
     * <p>
     * The seed is read-then-write over pod names and nothing stopped two instances doing it at once. The
     * <em>try</em> variant rather than the blocking one because the seed is idempotent reconciliation: if another
     * instance holds the lock it is already doing exactly this work, so the right move is to skip, not to queue
     * behind it and repeat it. A transaction-level lock is released with the transaction, including on rollback, so
     * a crashed instance cannot leave the seed wedged.
     * </p>
     *
     * @return whether this instance got the lock
     */
    @Query("select pg_try_advisory_xact_lock(:key)")
    boolean tryLockForSeeding(long key);

}
