package com.asrevo.cvhome.manager.controller;

import com.asrevo.cvhome.commons.annotation.OrgStorePrincipalInfo;
import com.asrevo.cvhome.commons.domain.Groups;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;
import com.asrevo.cvhome.keycloak.domain.user.*;
import com.asrevo.cvhome.keycloak.service.UserAccountService;
import com.asrevo.cvhome.keycloak.service.impl.KeycloakUserAccountServiceImpl;
import com.asrevo.cvhome.manager.commons.dto.ManagerStoreDto;
import com.asrevo.cvhome.manager.service.InternalStoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;

@RestController
@RequestMapping("api/v1/user-account")
@Slf4j
public class UserAccountController {
    private final UserAccountService userAccountService;
    public final InternalStoreService internalStoreService;

    public UserAccountController(OAuth2ResourceServerProperties properties, InternalStoreService internalStoreService) throws URISyntaxException {
        this.userAccountService = new KeycloakUserAccountServiceImpl(new URI(properties.getJwt().getJwkSetUri()));
        this.internalStoreService = internalStoreService;
    }

    @GetMapping("list")
    @PreAuthorize("hasPermission(#store,'ManagerStoreId','STORE.USERS.LIST')")
    public Mono<ReadableUserList> list(@OrgStorePrincipalInfo UserOrgStoreIdentity identity, @RequestParam ManagerStoreId store) {
        return Mono.just(userAccountService.list(new ListUsersQuery(identity.org(), store)));
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
    public Mono<ReadableUser> create(@OrgStorePrincipalInfo UserOrgStoreIdentity identity, @RequestParam ManagerStoreId store, @RequestBody PersistableUser create) {
        identity = createImpersonateIdentity(identity, store, "create-user");
        return Mono.just(userAccountService.createUser(identity, store, create));

    }


    @PostMapping("reset")
    @PreAuthorize("hasPermission(#store,'ManagerStoreId','STORE.USERS.RESET_PASSWORD')")
    public void resetPassword(@OrgStorePrincipalInfo UserOrgStoreIdentity identity, @RequestParam ManagerStoreId store, @RequestParam String userId, @RequestBody UserPassword passwordRequestDto) {
        identity = createImpersonateIdentity(identity, store, "reset-user-password");
        userAccountService.resetPassword(identity, store, passwordRequestDto, userId);
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
