package com.asrevo.cvhome.controlplane.manager.controller;

import java.security.Principal;
import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.commons.annotation.OrgStorePrincipalInfo;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;
import com.asrevo.cvhome.controlplane.manager.service.ManagedUserAccountService;
import com.asrevo.cvhome.uaa.domain.user.PersistableUser;
import com.asrevo.cvhome.uaa.domain.user.ReadableUser;
import com.asrevo.cvhome.uaa.domain.user.ReadableUserList;
import com.asrevo.cvhome.uaa.domain.user.UserPassword;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("api/v1/user-account")
@Slf4j
@AllArgsConstructor
public class UserAccountController {

    private final ManagedUserAccountService managedUserAccountService;

    @GetMapping("current")

    public ReadableUser current(@AuthenticationPrincipal Principal principal) {
        return managedUserAccountService.findOne(principal.getName());
    }

    @GetMapping("list")
    @PreAuthorize("hasPermission(#store,'ManagerStoreId','STORE-CORE.USERS.LIST')")

    public ReadableUserList list(@AuthenticationPrincipal Principal principal,
                                 @OrgStorePrincipalInfo UserOrgStoreIdentity identity, @RequestParam ManagerStoreId store,
                                 Pageable pageable) {
        return managedUserAccountService.list(identity, store, pageable);
    }

    @GetMapping("find-one")
    // @PreAuthorize("hasPermission(#store,'ManagerStoreId','STORE-CORE.USERS.LIST')")

    public ReadableUser findOne(@OrgStorePrincipalInfo UserOrgStoreIdentity identity,
                                @RequestParam ManagerStoreId store, @RequestParam String userId) {
        return managedUserAccountService.findOne(identity, store, userId);
    }

    @GetMapping("assignable-roles")

    public Set<String> assignableRoles() {
        return managedUserAccountService.getAssignableRoles();
    }

    @PostMapping("create")
    @PreAuthorize("hasPermission(#store,'ManagerStoreId','STORE-CORE.USERS.CREATE')")

    public ReadableUser create(@OrgStorePrincipalInfo UserOrgStoreIdentity identity,
                               @RequestParam ManagerStoreId store, @RequestBody PersistableUser user) {
        return managedUserAccountService.createUser(identity, store, user);
    }

    @PutMapping("update")
    @PreAuthorize("hasPermission(#store,'ManagerStoreId','STORE-CORE.USERS.UPDATE')")

    public ReadableUser update(@OrgStorePrincipalInfo UserOrgStoreIdentity identity,
                               @RequestParam ManagerStoreId store, @RequestBody PersistableUser user) {
        return managedUserAccountService.updateUser(identity, store, user);
    }

    @PostMapping("reset")
    @PreAuthorize("hasPermission(#store,'ManagerStoreId','STORE-CORE.USERS.RESET_PASSWORD')")

    public void resetPassword(@OrgStorePrincipalInfo UserOrgStoreIdentity identity,
                              @RequestParam ManagerStoreId store, @RequestParam String userId,
                              @RequestBody UserPassword passwordRequestDto) {
        managedUserAccountService.resetPassword(identity, store, userId, passwordRequestDto);
    }

    @DeleteMapping("delete")
    @PreAuthorize("hasPermission(#store,'ManagerStoreId','STORE-CORE.USERS.DELETE')")

    public void delete(@OrgStorePrincipalInfo UserOrgStoreIdentity identity, @RequestParam ManagerStoreId store,
                       @RequestParam String userId) {
        managedUserAccountService.deleteUser(identity, store, userId);
    }

    @PostMapping("enable")
    @PreAuthorize("hasPermission(#store,'ManagerStoreId','STORE-CORE.USERS.ENABLE')")

    public void enable(@OrgStorePrincipalInfo UserOrgStoreIdentity identity, @RequestParam ManagerStoreId store,
                       @RequestParam String userId) {
        managedUserAccountService.enableUser(identity, store, userId);
    }

    @PostMapping("disable")
    @PreAuthorize("hasPermission(#store,'ManagerStoreId','STORE-CORE.USERS.DISABLE')")

    public void disable(@OrgStorePrincipalInfo UserOrgStoreIdentity identity, @RequestParam ManagerStoreId store,
                        @RequestParam String userId) {
        managedUserAccountService.disableUser(identity, store, userId);
    }

}
