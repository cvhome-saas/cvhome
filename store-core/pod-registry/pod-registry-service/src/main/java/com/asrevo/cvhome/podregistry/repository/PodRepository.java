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
     * One page of pods, scoped to an organization and narrowed by a search term.
     *
     * <p>
     * The term spans the name and the endpoint. Those are the two things an operator has in hand when they come
     * looking — a pod named in an alert, or a host in a log line — and the id is not one of them, being an
     * ObjectId nobody reads out.
     * </p>
     *
     * <p>
     * {@code orgId} null means platform-wide: a super admin or a service principal. It is the same scoping
     * {@code findAllByOrgId} does, expressed in the query so that the search composes with it rather than
     * replacing it. The casts are needed because Postgres cannot infer the type of a bare null parameter.
     * </p>
     */
    @Query("""
            select * from pod_registry.pod
            where (cast(:orgId as varchar) is null or org_id = :orgId)
              and (cast(:term as varchar) is null
                   or name ilike '%' || :term || '%'
                   or endpoint ilike '%' || :term || '%')
            order by name limit :limit offset :offset""")
    List<PodEntity> findVisible(String orgId, String term, int limit, long offset);

    /** The matching total, for the page the query above returns a slice of. */
    @Query("""
            select count(*) from pod_registry.pod
            where (cast(:orgId as varchar) is null or org_id = :orgId)
              and (cast(:term as varchar) is null
                   or name ilike '%' || :term || '%'
                   or endpoint ilike '%' || :term || '%')""")
    long countVisible(String orgId, String term);

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
