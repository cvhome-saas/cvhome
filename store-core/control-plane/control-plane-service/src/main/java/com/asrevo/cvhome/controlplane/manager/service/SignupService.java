package com.asrevo.cvhome.controlplane.manager.service;

import com.asrevo.cvhome.controlplane.manager.dto.CreateOrgRequest;
import com.asrevo.cvhome.uaa.api.errors.UaaApiUnavailableException;
import com.asrevo.cvhome.uaa.api.errors.UaaConflictException;
import com.asrevo.cvhome.uaa.domain.user.ReadableUser;

public interface SignupService {

    /**
     * Creates the organization and its first user.
     *
     * @throws UaaConflictException        that email is already registered in uaa, so the organization must not be
     *                                     presented as created
     * @throws UaaApiUnavailableException  uaa could not be reached; the organization row may already exist, so this
     *                                     needs reconciling rather than retrying blindly
     */
    ReadableUser createOrgUser(CreateOrgRequest request) throws UaaConflictException, UaaApiUnavailableException;

}
