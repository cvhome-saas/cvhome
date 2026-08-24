package com.asrevo.cvhome.tenancy.manager.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.commons.domain.Email;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.tenancy.commons.dto.ListOrgQuery;
import com.asrevo.cvhome.tenancy.commons.dto.ManagerOrgDto;
import com.asrevo.cvhome.tenancy.errors.OrgNotFoundException;

public interface InternalOrgService {

    ManagerOrgId createOrgForUser(Email email);

    Page<ManagerOrgDto> findAll(Pageable pageable);

    /**
     * One page of organizations, narrowed by a search term and a status.
     *
     * <p>
     * Super-admin only at the controller, like everything else on this aggregate. A null query means no filter,
     * which is what the unfiltered overload above delegates to.
     * </p>
     */
    Page<ManagerOrgDto> findAll(ListOrgQuery query, Pageable pageable);

    /**
     * @throws OrgNotFoundException no organization exists with that id
     */
    ManagerOrgDto findOne(ManagerOrgId id) throws OrgNotFoundException;

    /**
     * Records which uaa account owns an organization.
     *
     * <p>
     * Written once, by {@code SignupServiceImpl}, immediately after uaa has created the first administrator — and
     * by {@code OrgOwnerBackfill} for the rows created before this existed. Nothing else assigns an owner, and
     * nothing reassigns one: transferring an organization is a different operation and does not exist.
     * </p>
     *
     * @throws OrgNotFoundException no organization exists with that id
     */
    void recordOwner(ManagerOrgId id, String ownerUserId) throws OrgNotFoundException;

}
