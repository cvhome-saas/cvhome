package com.asrevo.cvhome.tenancy.manager.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.tenancy.commons.dto.InvitationStatus;
import com.asrevo.cvhome.tenancy.manager.entity.OrgInvitationEntity;

@Repository
public interface OrgInvitationRepository extends CrudRepository<OrgInvitationEntity, StoreMerchantId> {

    /** Looked up by hash — the plaintext token is never stored, so this is the only way in. */
    Optional<OrgInvitationEntity> findByTokenHash(String tokenHash);

    List<OrgInvitationEntity> findByOrgId(ManagerOrgId orgId);

    Optional<OrgInvitationEntity> findByOrgIdAndEmailAndStatus(ManagerOrgId orgId, String email,
                                                               InvitationStatus status);

}
