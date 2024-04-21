package com.asrevo.cvhome.manager.controller;

import com.asrevo.cvhome.commons.annotation.OrgStorePrincipalInfo;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;
import com.asrevo.cvhome.manager.commons.dto.ListManagerStoreQuery;
import com.asrevo.cvhome.manager.commons.dto.ManagerStoreDto;
import com.asrevo.cvhome.manager.service.InternalStoreService;
import com.asrevo.cvhome.manager.service.StoreManagerService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("api/v1/store-manager")
@AllArgsConstructor
@Slf4j
public class StoreManagerController {
    private final StoreManagerService managerService;
    private final InternalStoreService internalStoreService;

    @PostMapping("list")
    public Mono<Page<ManagerStoreDto>> findAllStores(@OrgStorePrincipalInfo UserOrgStoreIdentity identity, @RequestBody ListManagerStoreQuery listManagerStoreQuery, Pageable pageable) {
        return Mono.just(internalStoreService.findAll(identity, listManagerStoreQuery, pageable));
    }

    @PostMapping("private/store")
    @PreAuthorize("hasAnyRole('ROLE_ORG_ADMIN')")
    public Mono<Void> create(@OrgStorePrincipalInfo UserOrgStoreIdentity identity, @RequestBody Map<Object, Object> request) {
        return this.managerService.createStore(identity.org(), request);
    }

    @GetMapping(value = "private/store/unique", params = "name")
    @PreAuthorize("hasAnyRole('ROLE_ORG_ADMIN')")
    public Mono<Map<String, Boolean>> checkExist(@RequestParam("name") String name) {
        return Mono.just(Map.of("exists", internalStoreService.checkNameExists(name)));
    }

    @GetMapping("private/store")
    @PreAuthorize("hasAnyRole('ROLE_ORG_ADMIN')")
    public Mono<PageImpl<Object>> findAllStoresDetailed(@OrgStorePrincipalInfo UserOrgStoreIdentity identity, Pageable pageable) {
        return managerService.findAll(identity, new ListManagerStoreQuery(null, null, null), pageable);
    }

    @GetMapping("private/store/{code}")
    public Mono<Object> getStoreDetailed(@OrgStorePrincipalInfo UserOrgStoreIdentity identity, @PathVariable("code") ManagerStoreId managerStoreId) {
        return managerService.getStore(identity, managerStoreId);
    }

    @GetMapping("store-info")
    @PreAuthorize("hasPermission(#store,'ManagerStoreId','STORE.FIND-ONE')")
    public Mono<ManagerStoreDto> storeInfo(@OrgStorePrincipalInfo UserOrgStoreIdentity identity, @RequestParam ManagerStoreId store) {
        return Mono.just(internalStoreService.findStore(store));
    }
}
