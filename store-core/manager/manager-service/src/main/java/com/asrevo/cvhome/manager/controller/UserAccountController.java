package com.asrevo.cvhome.manager.controller;

import com.asrevo.cvhome.commons.annotation.OrgStorePrincipalInfo;
import com.asrevo.cvhome.commons.domain.Groups;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;
import com.asrevo.cvhome.keycloak.domain.user.PersistableUser;
import com.asrevo.cvhome.keycloak.domain.user.ReadableUser;
import com.asrevo.cvhome.keycloak.domain.user.ReadableUserList;
import com.asrevo.cvhome.keycloak.domain.user.UserPassword;
import com.asrevo.cvhome.keycloak.service.UserAccountService;
import com.asrevo.cvhome.manager.commons.dto.ManagerStoreDto;
import com.asrevo.cvhome.manager.service.InternalStoreService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.security.Principal;

@RestController
@RequestMapping("api/v1/user-account")
@Slf4j
@AllArgsConstructor
public class UserAccountController {
    public final InternalStoreService internalStoreService;
    private final UserAccountService userAccountService;


    @GetMapping("current")
    public Mono<ReadableUser> current(@AuthenticationPrincipal Principal principal) {
        return Mono.just(userAccountService.current(principal.getName()));
    }

    @GetMapping("list")
    @PreAuthorize("hasPermission(#store,'ManagerStoreId','STORE.USERS.LIST')")
    public Mono<ReadableUserList> list(@AuthenticationPrincipal Principal principal, @OrgStorePrincipalInfo UserOrgStoreIdentity identity, @RequestParam ManagerStoreId store) {
        UserOrgStoreIdentity impersonateIdentity = createImpersonateIdentity(identity, store, "list-users");
        return Mono.just(userAccountService.list(principal, impersonateIdentity, store));
    }

    @GetMapping("find-one")
//    @PreAuthorize("hasPermission(#store,'ManagerStoreId','STORE.USERS.LIST')")
    public Mono<ReadableUser> findOne(@OrgStorePrincipalInfo UserOrgStoreIdentity identity, @RequestParam String userId) {
        return Mono.just(userAccountService.findOne(identity, userId));
    }

    @GetMapping("groups")
    public Mono<Groups[]> groups() {
        return Mono.just(Groups.values());
    }

    @PostMapping("create")
    @PreAuthorize("hasPermission(#store,'ManagerStoreId','STORE.USERS.CREATE')")
    public Mono<ReadableUser> create(@OrgStorePrincipalInfo UserOrgStoreIdentity identity, @RequestParam ManagerStoreId store, @RequestBody PersistableUser user) {
        identity = createImpersonateIdentity(identity, store, "create-user");
        return Mono.just(userAccountService.createManagedUser(identity, store, user));

    }

    @PutMapping("update")
    @PreAuthorize("hasPermission(#store,'ManagerStoreId','STORE.USERS.UPDATE')")
    public Mono<ReadableUser> update(@OrgStorePrincipalInfo UserOrgStoreIdentity identity, @RequestParam ManagerStoreId store, @RequestBody PersistableUser user) {
        UserOrgStoreIdentity impersonateIdentity = createImpersonateIdentity(identity, store, "update-user");
        return Mono.just(userAccountService.updateManagedUser(impersonateIdentity, store, user));
//        {"firstName":"12313","lastName":"55555","userName":"org1-store1-moderator","emailAddress":"sfds@dfsf.vv","password":"","repeatPassword":"","active":true,"groups":[{"name":"STORE_MODERATOR"}],"id":"3dea29fd-f6b2-48b1-8231-f4b5f1c68715"}
    }

    @PostMapping("reset")
    @PreAuthorize("hasPermission(#store,'ManagerStoreId','STORE.USERS.RESET_PASSWORD')")
    public void resetPassword(@OrgStorePrincipalInfo UserOrgStoreIdentity identity, @RequestParam ManagerStoreId store, @RequestParam String userId, @RequestBody UserPassword passwordRequestDto) {
        identity = createImpersonateIdentity(identity, store, "reset-user-password");
        userAccountService.resetPassword(identity, store, passwordRequestDto, userId, false);
    }

    @DeleteMapping("delete")
    @PreAuthorize("hasPermission(#store,'ManagerStoreId','STORE.USERS.DELETE')")
    public Mono<Void> delete(@OrgStorePrincipalInfo UserOrgStoreIdentity identity, @RequestParam ManagerStoreId store, @RequestParam String userId) {
        identity = createImpersonateIdentity(identity, store, "delete-user");
        userAccountService.deleteUser(identity, store, userId);
        return Mono.empty();
    }

    @PostMapping("enable")
    @PreAuthorize("hasPermission(#store,'ManagerStoreId','STORE.USERS.ENABLE')")
    public Mono<Object> enable(@OrgStorePrincipalInfo UserOrgStoreIdentity identity, @RequestParam ManagerStoreId store, @RequestParam String userId) {
        identity = createImpersonateIdentity(identity, store, "enable-user");
        userAccountService.enableUser(identity, store, userId);
        return Mono.empty();
    }

    @PostMapping("disable")
    @PreAuthorize("hasPermission(#store,'ManagerStoreId','STORE.USERS.DISABLE')")
    public Mono<Object> disable(@OrgStorePrincipalInfo UserOrgStoreIdentity identity, @RequestParam ManagerStoreId store, @RequestParam String userId) {
        identity = createImpersonateIdentity(identity, store, "disable-user");
        userAccountService.disableUser(identity, store, userId);
        return Mono.empty();
    }

    private UserOrgStoreIdentity createImpersonateIdentity(UserOrgStoreIdentity identity, ManagerStoreId store, String action) {
        if (identity.isSuperAdmin()) {
            ManagerStoreDto s = internalStoreService.findStore(store);
            log.info("Impersonating user {} for action {} to org {} with store {}", identity, action, s.owner(), s.id().id().toString());
            return new UserOrgStoreIdentity(s.owner(), s.id().id().toString(), identity.roles());
        } else {
            return identity;
        }
    }
}
