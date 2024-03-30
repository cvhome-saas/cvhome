package com.asrevo.cvhome.manager.controller;

import com.asrevo.cvhome.commons.annotation.OrgStorePrincipalInfo;
import com.asrevo.cvhome.commons.domain.Groups;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.UserOrgStoreInfo;
import com.asrevo.cvhome.commons.utils.OperationExecution;
import com.asrevo.cvhome.manager.commons.dto.CreateUserRequestDto;
import com.asrevo.cvhome.manager.commons.dto.KeyCloakUserDto;
import com.asrevo.cvhome.manager.commons.dto.ListUsersQuery;
import com.asrevo.cvhome.manager.commons.dto.RestPasswordRequestDto;
import com.asrevo.cvhome.manager.service.UserAccountService;
import com.asrevo.cvhome.manager.utils.ErrorCodes;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("api/v1/user-account")
@AllArgsConstructor
@Slf4j
public class UserAccountController {
    private final UserAccountService userAccountService;

    @GetMapping("list")
    @PreAuthorize("hasPermission(#storeId,'ManagerStoreId','STORE.USERS.LIST')")
    public Mono<List<KeyCloakUserDto>> list(@OrgStorePrincipalInfo UserOrgStoreInfo info, @RequestParam ManagerStoreId storeId) {
        return Mono.just(userAccountService.list(new ListUsersQuery(info.org(), storeId)));
    }

    @PostMapping("create")
    @PreAuthorize("hasPermission(#storeId,'ManagerStoreId','STORE.USERS.CREATE')")
    public Mono<KeyCloakUserDto> create(@OrgStorePrincipalInfo UserOrgStoreInfo info, @RequestParam ManagerStoreId storeId, @RequestBody CreateUserRequestDto create) {
        if (create.groups() == null || create.groups().isEmpty()) {
            throw new OperationExecution(ErrorCodes.groups_should_not_be_empty);
        }
        if (create.groups().contains(Groups.CUSTOMER)) {
            throw new OperationExecution(ErrorCodes.create_customer_not_allowed);
        }
        if (create.groups().contains(Groups.ORG_ADMIN)) {
            throw new OperationExecution(ErrorCodes.create_org_admin_not_allowed);
        }
        if (create.groups().contains(Groups.SUPER_ADMIN)) {
            throw new OperationExecution(ErrorCodes.create_super_admin_not_allowed);
        }
        if (userAccountService.usernameExist(create.username())) {
            throw new OperationExecution(ErrorCodes.username_already_taken);
        }
        return Mono.just(userAccountService.createUser(info.org(), storeId, create));
    }

    @PostMapping("reset")
    @PreAuthorize("hasPermission(#storeId,'ManagerStoreId','STORE.USERS.RESET_PASSWORD')")
    public void resetPassword(@OrgStorePrincipalInfo UserOrgStoreInfo info, @RequestParam ManagerStoreId storeId, @RequestParam String userId, @RequestBody RestPasswordRequestDto passwordRequestDto) {
        userAccountService.resetPassword(info, storeId, passwordRequestDto, userId);
    }

    @DeleteMapping("delete")
    @PreAuthorize("hasPermission(#storeId,'ManagerStoreId','STORE.USERS.DELETE')")
    public Mono<Void> delete(@OrgStorePrincipalInfo UserOrgStoreInfo info, @RequestParam ManagerStoreId storeId, @RequestParam String userId) {
        userAccountService.deleteUser(info, storeId, userId);
        return Mono.empty();
    }

    @PostMapping("enable")
    @PreAuthorize("hasPermission(#storeId,'ManagerStoreId','STORE.USERS.ENABLE')")
    public Mono<Object> enable(@OrgStorePrincipalInfo UserOrgStoreInfo info, @RequestParam ManagerStoreId storeId, @RequestParam String userId) {
        userAccountService.enableUser(info, storeId, userId);
        return Mono.empty();
    }

    @PostMapping("disable")
    @PreAuthorize("hasPermission(#storeId,'ManagerStoreId','STORE.USERS.DISABLE')")
    public Mono<Object> disable(@OrgStorePrincipalInfo UserOrgStoreInfo info, @RequestParam ManagerStoreId storeId, @RequestParam String userId) {
        userAccountService.disableUser(info, storeId, userId);
        return Mono.empty();
    }

}
