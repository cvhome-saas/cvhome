package com.asrevo.cvhome.store.controller;

import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.store.commons.dto.CreateStoreRequest;
import com.asrevo.cvhome.store.commons.dto.StoreDto;
import com.asrevo.cvhome.store.entity.OwnerEntity;
import com.asrevo.cvhome.store.entity.StoreEntity;
import com.asrevo.cvhome.store.service.OwnerService;
import com.asrevo.cvhome.store.service.StoreService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("api/v1/store")
@AllArgsConstructor
@Slf4j
public class StoreController {
    private final StoreService storeService;
    private final OwnerService ownerService;

    @PostMapping
    public StoreDto create(@RequestBody CreateStoreRequest request, @AuthenticationPrincipal Principal principal) {
        OwnerEntity owner = ownerService.getOwnerOrCreate(IdentityId.of(principal.getName()), principal);
       return storeService.createStore(request, owner.getId());
    }

    @GetMapping("me")
    public List<StoreDto> findAllStores(@AuthenticationPrincipal Principal principal) {
        return storeService.findAll(IdentityId.of(principal.getName()));
    }
}
