package com.asrevo.cvhome.manager.controller;

import com.asrevo.cvhome.commons.annotation.OrgStorePrincipalInfo;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;
import com.asrevo.cvhome.manager.commons.dto.CreateManagerStoreRequest;
import com.asrevo.cvhome.manager.commons.dto.CreateManagerStoreResponse;
import com.asrevo.cvhome.manager.commons.dto.ListManagerStoreQuery;
import com.asrevo.cvhome.manager.commons.dto.ManagerStoreDto;
import com.asrevo.cvhome.manager.service.InternalStoreService;
import com.asrevo.cvhome.manager.service.StoreManagerService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/v1/store-manager")
@AllArgsConstructor
@Slf4j
public class StoreManagerController {
    private final StoreManagerService managerService;
    private final InternalStoreService storeService;

    @PostMapping("list")
    public Mono<Page<ManagerStoreDto>> findAllStores(@OrgStorePrincipalInfo UserOrgStoreIdentity identity, @RequestBody ListManagerStoreQuery listManagerStoreQuery, Pageable pageable) {
        return Mono.just(storeService.findAll(identity, listManagerStoreQuery, pageable));
    }

    @PostMapping("create")
    @PreAuthorize("hasAnyRole('ROLE_ORG_ADMIN')")
    public Mono<CreateManagerStoreResponse> create(@RequestBody CreateManagerStoreRequest request, @OrgStorePrincipalInfo UserOrgStoreIdentity identity) {
        return Mono.just(managerService.createStore(request, identity.org()));
    }

    @GetMapping("store-info")
    @PreAuthorize("hasPermission(#store,'ManagerStoreId','STORE.FIND-ONE')")
    public Mono<ManagerStoreDto> storeInfo(@OrgStorePrincipalInfo UserOrgStoreIdentity identity, @RequestParam ManagerStoreId store) {
        return Mono.just(storeService.findStore(store));
    }
}
