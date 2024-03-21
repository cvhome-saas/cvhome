package com.asrevo.cvhome.manager.controller;

import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.manager.commons.dto.CreateManagerStoreRequest;
import com.asrevo.cvhome.manager.commons.dto.CreateManagerStoreResponse;
import com.asrevo.cvhome.manager.commons.dto.ManagerStoreDto;
import com.asrevo.cvhome.manager.service.InternalStoreService;
import com.asrevo.cvhome.manager.service.StoreManagerService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("api/v1/manager-store")
@AllArgsConstructor
@Slf4j
public class ManagerStoreController {
    private final StoreManagerService managerService;
    private final InternalStoreService storeService;

    @PostMapping
    public CreateManagerStoreResponse create(@RequestBody CreateManagerStoreRequest request, @AuthenticationPrincipal Principal principal) {
        return managerService.createStore(request, IdentityId.of(principal.getName()));
    }

    @PostMapping("find-all")
    public Page<ManagerStoreDto> findAllStores(@AuthenticationPrincipal Principal principal, @RequestBody ManagerStoreDto managerStoreDto, Pageable pageable) {
        return storeService.findAll(managerStoreDto, IdentityId.of(principal.getName()), pageable);
    }
}
