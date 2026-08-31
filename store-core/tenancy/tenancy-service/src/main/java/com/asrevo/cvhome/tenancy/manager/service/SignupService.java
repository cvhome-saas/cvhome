package com.asrevo.cvhome.tenancy.manager.service;

import com.asrevo.cvhome.tenancy.errors.DuplicateSignupEmailException;
import com.asrevo.cvhome.tenancy.manager.dto.CreateOrgRequest;
import com.asrevo.cvhome.uaa.api.errors.UaaApiUnavailableException;
import com.asrevo.cvhome.uaa.domain.user.ReadableUser;

public interface SignupService {

    /**
     * Creates the organization and its first administrator, or neither.
     *
     * <p>
     * {@code UaaConflictException} used to be on this signature and is not any more. It is a
     * {@code RemoteServiceException} carrying uaa's own generic {@code COMMON.DATA_INTEGRITY_VIOLATION}, which is
     * also what an over-long value produces — so a caller could not tell "that address is taken" from "something
     * else about this row was refused", and the console guessed. The condition has a name now; the remote failure
     * stays inside this service, where it came from.
     * </p>
     *
     * @throws DuplicateSignupEmailException that address already has an account, so no organization was created
     * @throws UaaApiUnavailableException    uaa could not be reached and it is unknown whether it created the user;
     *                                       the organization is rolled back either way
     */
    ReadableUser createOrgUser(CreateOrgRequest request)
            throws DuplicateSignupEmailException, UaaApiUnavailableException;

}
