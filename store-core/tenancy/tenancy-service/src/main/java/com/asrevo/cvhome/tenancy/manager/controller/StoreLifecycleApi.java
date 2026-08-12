package com.asrevo.cvhome.tenancy.manager.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.commons.annotation.OrgStorePrincipalInfo;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;
import com.asrevo.cvhome.tenancy.commons.dto.ManagerStoreDto;
import com.asrevo.cvhome.tenancy.errors.IllegalLifecycleTransitionException;
import com.asrevo.cvhome.tenancy.errors.StoreNotFoundException;
import com.asrevo.cvhome.tenancy.manager.service.StoreLifecycleService;

import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.AllArgsConstructor;

/**
 * Opening and closing a store.
 *
 * <p>
 * Suspend and resume are the operator's lever and are super-admin only — suspending takes a merchant's business
 * offline, which is not something their own admin should be able to do to themselves by accident, nor something
 * they can undo unilaterally. Archive and delete belong to the owner, so they carry the store-scoped permission
 * token instead.
 * </p>
 */
@RestController
@RequestMapping("api/v1/store-manager")
@AllArgsConstructor
@Tag(name = "Store lifecycle", description = "Suspend, resume, archive and delete a store")
public class StoreLifecycleApi {

    private static final String OWNER = "hasPermission(#store,'ManagerStoreId','STORE-CORE.STORE-FIND-ONE')";

    private final StoreLifecycleService lifecycleService;

    @PostMapping("private/store/suspend")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ManagerStoreDto suspend(@OrgStorePrincipalInfo UserOrgStoreIdentity identity,
                                   @RequestParam ManagerStoreId store,
                                   @RequestParam(required = false) String reason, Authentication authentication)
            throws StoreNotFoundException, IllegalLifecycleTransitionException {
        return lifecycleService.suspend(identity, store, actorOf(authentication),
                reason == null ? "suspended by operator" : reason);
    }

    @PostMapping("private/store/resume")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ManagerStoreDto resume(@OrgStorePrincipalInfo UserOrgStoreIdentity identity,
                                  @RequestParam ManagerStoreId store, Authentication authentication)
            throws StoreNotFoundException, IllegalLifecycleTransitionException {
        return lifecycleService.resume(identity, store, actorOf(authentication));
    }

    @PostMapping("private/store/archive")
    @PreAuthorize(OWNER)
    public ManagerStoreDto archive(@OrgStorePrincipalInfo UserOrgStoreIdentity identity,
                                   @RequestParam ManagerStoreId store, Authentication authentication)
            throws StoreNotFoundException, IllegalLifecycleTransitionException {
        return lifecycleService.archive(identity, store, actorOf(authentication));
    }

    /**
     * Soft delete: the row stays and only the status changes, because billing holds a subscription against this
     * id and the pod registry holds a placement. A hard delete would orphan both.
     */
    @DeleteMapping("private/store")
    @PreAuthorize(OWNER)
    public ManagerStoreDto delete(@OrgStorePrincipalInfo UserOrgStoreIdentity identity,
                                  @RequestParam ManagerStoreId store, Authentication authentication)
            throws StoreNotFoundException, IllegalLifecycleTransitionException {
        return lifecycleService.delete(identity, store, actorOf(authentication));
    }

    private static String actorOf(Authentication authentication) {
        return authentication == null ? "unknown" : authentication.getName();
    }

}
