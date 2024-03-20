package com.asrevo.cvhome.manager.controller;

import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.manager.commons.dto.CreateManagerStoreRequest;
import com.asrevo.cvhome.manager.commons.dto.CreateStoreResponse;
import com.asrevo.cvhome.manager.commons.dto.ManagerStoreDto;
import com.asrevo.cvhome.manager.service.InternalStoreService;
import com.asrevo.cvhome.manager.service.StoreManagerService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.awt.print.Pageable;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("api/v1/manager-store")
@AllArgsConstructor
@Slf4j
public class ManagerStoreController {
    private final StoreManagerService managerService;
    private final InternalStoreService storeService;

    @PostMapping
    public CreateStoreResponse create(@RequestBody CreateManagerStoreRequest request, @AuthenticationPrincipal Principal principal) {
        return managerService.createStore(request, IdentityId.of(principal.getName()));
    }

    @GetMapping("find-all")
    public List<ManagerStoreDto> findAllStores(@AuthenticationPrincipal Principal principal, Pageable pageable) {
        return storeService.findAll(IdentityId.of(principal.getName()), pageable);
    }
}
