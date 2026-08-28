package com.asrevo.cvhome.tenancy.manager.service;

import java.util.Set;

import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;
import com.asrevo.cvhome.tenancy.errors.ForeignOrgUserAccessException;
import com.asrevo.cvhome.tenancy.errors.ForeignStoreUserAccessException;
import com.asrevo.cvhome.tenancy.errors.ManagedUserNotFoundException;
import com.asrevo.cvhome.uaa.api.errors.UaaApiUnavailableException;
import com.asrevo.cvhome.uaa.api.errors.UaaConflictException;
import com.asrevo.cvhome.uaa.api.errors.UaaOperationForbiddenException;
import com.asrevo.cvhome.uaa.domain.user.PersistableUser;
import com.asrevo.cvhome.uaa.domain.user.ReadableUser;
import com.asrevo.cvhome.uaa.domain.user.ReadableUserList;
import com.asrevo.cvhome.uaa.domain.user.UserPassword;

/**
 * Administering uaa users on behalf of an organization.
 *
 * <p>
 * Every method that reaches an existing user declares the same three failures, and that repetition is the contract:
 * uaa enforces no tenancy of its own, so "does this user exist" and "is this user yours" are decided here, on every
 * call, and a caller cannot compile without deciding what each means.
 * </p>
 */
public interface ManagedUserAccountService {

    ReadableUser findOne(String id) throws ManagedUserNotFoundException, UaaApiUnavailableException;

    ReadableUserList list(UserOrgStoreIdentity identity, StoreMerchantId store, Pageable pageable)
            throws UaaApiUnavailableException;

    ReadableUser findOne(UserOrgStoreIdentity identity, StoreMerchantId store, String userId)
            throws ManagedUserNotFoundException, ForeignOrgUserAccessException, ForeignStoreUserAccessException,
            UaaApiUnavailableException;

    ReadableUser createUser(UserOrgStoreIdentity identity, StoreMerchantId store, PersistableUser user)
            throws UaaConflictException, UaaApiUnavailableException;

    ReadableUser updateUser(UserOrgStoreIdentity identity, StoreMerchantId store, PersistableUser user)
            throws ManagedUserNotFoundException, ForeignOrgUserAccessException, ForeignStoreUserAccessException,
            UaaConflictException, UaaApiUnavailableException;

    void resetPassword(UserOrgStoreIdentity identity, StoreMerchantId store, String userId,
                       UserPassword passwordRequestDto)
            throws ManagedUserNotFoundException, ForeignOrgUserAccessException, ForeignStoreUserAccessException,
            UaaApiUnavailableException;

    void deleteUser(UserOrgStoreIdentity identity, StoreMerchantId store, String userId)
            throws ManagedUserNotFoundException, ForeignOrgUserAccessException, ForeignStoreUserAccessException,
            UaaOperationForbiddenException, UaaApiUnavailableException;

    void enableUser(UserOrgStoreIdentity identity, StoreMerchantId store, String userId)
            throws ManagedUserNotFoundException, ForeignOrgUserAccessException, ForeignStoreUserAccessException,
            UaaOperationForbiddenException, UaaApiUnavailableException;

    void disableUser(UserOrgStoreIdentity identity, StoreMerchantId store, String userId)
            throws ManagedUserNotFoundException, ForeignOrgUserAccessException, ForeignStoreUserAccessException,
            UaaOperationForbiddenException, UaaApiUnavailableException;

    Set<String> getAssignableRoles() throws UaaApiUnavailableException;

}
