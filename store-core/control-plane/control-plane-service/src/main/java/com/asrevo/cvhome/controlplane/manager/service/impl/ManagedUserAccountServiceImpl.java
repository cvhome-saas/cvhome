package com.asrevo.cvhome.controlplane.manager.service.impl;

import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;
import com.asrevo.cvhome.controlplane.errors.ForeignOrgUserAccessException;
import com.asrevo.cvhome.controlplane.errors.ForeignStoreUserAccessException;
import com.asrevo.cvhome.controlplane.errors.ManagedUserNotFoundException;
import com.asrevo.cvhome.controlplane.manager.service.ManagedUserAccountService;
import com.asrevo.cvhome.uaa.api.errors.UaaApiUnavailableException;
import com.asrevo.cvhome.uaa.api.errors.UaaConflictException;
import com.asrevo.cvhome.uaa.api.errors.UaaOperationForbiddenException;
import com.asrevo.cvhome.uaa.api.errors.UaaUserNotFoundException;
import com.asrevo.cvhome.uaa.domain.user.PersistableUser;
import com.asrevo.cvhome.uaa.domain.user.ReadableUser;
import com.asrevo.cvhome.uaa.domain.user.ReadableUserList;
import com.asrevo.cvhome.uaa.domain.user.UserPassword;
import com.asrevo.cvhome.uaa.service.UserAccountService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class ManagedUserAccountServiceImpl implements ManagedUserAccountService {

    private final UserAccountService userAccountService;

    /**
     * The one lookup, and therefore the one place uaa's "no such user" becomes control-plane's own.
     *
     * <p>
     * It used to null-check the result instead, which could never fire: the SDK threw on a 404 long before a null
     * could be returned, so a missing user reached the client as a 500 and this service's 404 was unreachable code.
     * </p>
     */
    @Override
    public ReadableUser findOne(String id) throws ManagedUserNotFoundException, UaaApiUnavailableException {
        try {
            return userAccountService.findOne(id);
        } catch (UaaUserNotFoundException e) {
            throw ManagedUserNotFoundException.of(id, e);
        }
    }

    /**
     * Restates uaa's "no such user" as this service's own, for the writes that happen after the guard has already
     * read the user: uaa remains the authority, and a user can be deleted between the two calls.
     */
    private static <T, E extends Throwable> T restatingNotFound(String userId, UaaCall<T, E> call)
            throws ManagedUserNotFoundException, UaaApiUnavailableException, E {
        try {
            return call.run();
        } catch (UaaUserNotFoundException e) {
            throw ManagedUserNotFoundException.of(userId, e);
        }
    }

    @Override
    public ReadableUserList list(UserOrgStoreIdentity identity, ManagerStoreId store, Pageable pageable)
            throws UaaApiUnavailableException {
        return userAccountService.list(
                Map.of("org", identity.org().id().toString(), "store", store.id().toString()), pageable.getPageNumber(),
                pageable.getPageSize());
    }

    @Override
    public ReadableUser findOne(UserOrgStoreIdentity identity, ManagerStoreId store, String userId)
            throws ManagedUserNotFoundException, ForeignOrgUserAccessException, ForeignStoreUserAccessException,
            UaaApiUnavailableException {
        ReadableUser user = findOne(userId);
        validateUserAccess(identity, store, user);
        return user;
    }

    @Override
    public ReadableUser createUser(UserOrgStoreIdentity identity, ManagerStoreId store, PersistableUser user)
            throws UaaConflictException, UaaApiUnavailableException {
        user.setActive(true);
        user.setOrg(identity.org().id().toString());
        user.setStore(store.id().toString());
        return userAccountService.createUser(user);
    }

    @Override
    public ReadableUser updateUser(UserOrgStoreIdentity identity, ManagerStoreId store, PersistableUser user)
            throws ManagedUserNotFoundException, ForeignOrgUserAccessException, ForeignStoreUserAccessException,
            UaaConflictException, UaaApiUnavailableException {
        ReadableUser existingUser = findOne(user.getId());
        validateUserAccess(identity, store, existingUser);
        // Ensure the user is not moved to another org/store via update
        user.setOrg(identity.org().id().toString());
        user.setStore(store.id().toString());
        return restatingNotFound(user.getId(), () -> userAccountService.updateUser(user));
    }

    @Override
    public void resetPassword(UserOrgStoreIdentity identity, ManagerStoreId store, String userId,
                              UserPassword passwordRequestDto)
            throws ManagedUserNotFoundException, ForeignOrgUserAccessException, ForeignStoreUserAccessException,
            UaaApiUnavailableException {
        ReadableUser user = findOne(userId);
        validateUserAccess(identity, store, user);
        restatingNotFound(userId, () -> {
            userAccountService.changePassword(userId, passwordRequestDto);
            return null;
        });
    }

    @Override
    public void deleteUser(UserOrgStoreIdentity identity, ManagerStoreId store, String userId)
            throws ManagedUserNotFoundException, ForeignOrgUserAccessException, ForeignStoreUserAccessException,
            UaaOperationForbiddenException, UaaApiUnavailableException {
        ReadableUser user = findOne(userId);
        validateUserAccess(identity, store, user);
        restatingNotFound(userId, () -> {
            userAccountService.deleteUser(userId);
            return null;
        });
    }

    @Override
    public void enableUser(UserOrgStoreIdentity identity, ManagerStoreId store, String userId)
            throws ManagedUserNotFoundException, ForeignOrgUserAccessException, ForeignStoreUserAccessException,
            UaaOperationForbiddenException, UaaApiUnavailableException {
        ReadableUser user = findOne(userId);
        validateUserAccess(identity, store, user);
        restatingNotFound(userId, () -> {
            userAccountService.enableUser(userId);
            return null;
        });
    }

    @Override
    public void disableUser(UserOrgStoreIdentity identity, ManagerStoreId store, String userId)
            throws ManagedUserNotFoundException, ForeignOrgUserAccessException, ForeignStoreUserAccessException,
            UaaOperationForbiddenException, UaaApiUnavailableException {
        ReadableUser user = findOne(userId);
        validateUserAccess(identity, store, user);
        restatingNotFound(userId, () -> {
            userAccountService.disableUser(userId);
            return null;
        });
    }

    @Override
    public Set<String> getAssignableRoles() throws UaaApiUnavailableException {
        return userAccountService.getAssignableRoles();
    }

    /**
     * The tenancy guard. uaa keeps {@code org} and {@code store} as free-form user metadata and enforces neither, so
     * this method is the only thing standing between one organization and another's user records.
     *
     * <p>
     * Each condition has its own type, so a caller — and a log — can tell "not yours" from "wrong store on the
     * request", which the identical {@code ResponseStatusException}s here could not. The third condition, "no such
     * user", moved to {@link #findOne(String)}, which is the frame that knows the id.
     * </p>
     */
    private void validateUserAccess(UserOrgStoreIdentity identity, ManagerStoreId store, ReadableUser user)
            throws ForeignOrgUserAccessException, ForeignStoreUserAccessException {

        if (!identity.org().id().toString().equals(user.getOrg())) {
            log.error("Access denied: User {} belongs to org {}, but admin belongs to org {}", user.getId(),
                    user.getOrg(), identity.org());
            throw ForeignOrgUserAccessException.of(user.getId(), user.getOrg(), identity.org().id().toString());
        }
        if (!store.id().toString().equals(user.getStore())) {
            log.error("Access denied: User {} belongs to store {}, but request is for store {}", user.getId(),
                    user.getStore(), store);
            throw ForeignStoreUserAccessException.of(user.getId(), user.getStore(), store.id().toString());
        }
    }

    /**
     * A uaa call that may report a missing user.
     *
     * <p>
     * {@code E} carries whatever else the particular call can throw — a conflict on update, the super-admin guard on
     * delete — straight through to the caller, so one helper serves every write without widening any signature to
     * failures that write cannot produce.
     * </p>
     */
    @FunctionalInterface
    private interface UaaCall<T, E extends Throwable> {

        T run() throws UaaUserNotFoundException, UaaApiUnavailableException, E;

    }

}
