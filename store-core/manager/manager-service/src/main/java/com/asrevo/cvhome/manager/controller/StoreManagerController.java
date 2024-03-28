package com.asrevo.cvhome.manager.controller;

import com.asrevo.cvhome.commons.annotation.OrgStorePrincipalInfo;
import com.asrevo.cvhome.commons.domain.UserOrgStoreInfo;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/v1/store-manager")
@AllArgsConstructor
@Slf4j
public class StoreManagerController {
    private final StoreManagerService managerService;
    private final InternalStoreService storeService;

    @PostMapping("list")
    public Mono<Page<ManagerStoreDto>> findAllStores(@OrgStorePrincipalInfo UserOrgStoreInfo info, @RequestBody ListManagerStoreQuery listManagerStoreQuery, Pageable pageable) {
        return Mono.just(storeService.findAll(listManagerStoreQuery, info.org(), pageable));
    }

    @PostMapping("create")
    @PreAuthorize("hasAnyRole('ROLE_ORG_ADMIN')")
    public Mono<CreateManagerStoreResponse> create(@RequestBody CreateManagerStoreRequest request, @OrgStorePrincipalInfo UserOrgStoreInfo info) {
        return Mono.just(managerService.createStore(request, info.org()));
    }
}
