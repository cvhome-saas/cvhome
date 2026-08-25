package com.asrevo.cvhome.tenancy.manager.repository;

import java.util.List;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.Repository;

import com.asrevo.cvhome.tenancy.manager.entity.OrgMemberEntity;

/**
 * Membership rows, keyed by {@code (org_id, user_id)}.
 *
 * <p>
 * Extends the marker {@code Repository} rather than {@code CrudRepository}: the table has a composite key, which
 * Spring Data JDBC cannot express as an aggregate id, so writes are explicit statements. That is enough for a
 * join table and avoids pretending it is an aggregate.
 * </p>
 */
public interface OrgMemberRepository extends Repository<OrgMemberEntity, String> {

    @Query("select * from tenancy.org_member where org_id = :orgId order by added_at")
    List<OrgMemberEntity> findByOrg(String orgId);

    @Query("select count(*) from tenancy.org_member where org_id = :orgId and user_id = :userId")
    long countMembership(String orgId, String userId);

    /**
     * Adds a member, tolerating one who is already there.
     *
     * @return 1 if newly added, 0 if the membership already existed
     */
    @Modifying
    @Query("""
            insert into tenancy.org_member (org_id, user_id, role, added_at, added_by)
            values (:orgId, :userId, :role, now(), :addedBy)
            on conflict (org_id, user_id) do nothing""")
    int add(String orgId, String userId, String role, String addedBy);

    @Modifying
    @Query("delete from tenancy.org_member where org_id = :orgId and user_id = :userId")
    int remove(String orgId, String userId);

}
