package com.asrevo.cvhome.tenancy.manager.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.commons.annotation.OrgStorePrincipalInfo;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;
import com.asrevo.cvhome.tenancy.errors.StoreNotFoundException;
import com.asrevo.cvhome.tenancy.manager.service.InternalStoreService;
import com.asrevo.cvhome.tenancy.org.service.PodService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("api/v1/router")
@AllArgsConstructor
@Slf4j
public class RouterController {

    public final InternalStoreService internalStoreService;

    private final PodService podService;

    /**
     * Which pod hosts a store. Guarded twice on purpose: the permission gate, and an org check inside
     * {@link InternalStoreService#getStorePod(UserOrgStoreIdentity, ManagerStoreId)} — the gate alone does not hold
     * a foreign store out, because the shared {@code isOrgAdmin} ignores the store it is asked about.
     *
     * <p>
     * It had no annotation at all, so any authenticated principal could map any store id to its pod endpoint.
     * </p>
     */
    @GetMapping("store-pod-by-store-id")
    @PreAuthorize("hasPermission(#store,'ManagerStoreId','STORE-CORE.STORE-FIND-ONE')")
    public Pod getStorePodByStoreId(@OrgStorePrincipalInfo UserOrgStoreIdentity identity,
                                    @RequestParam ManagerStoreId store) throws StoreNotFoundException {
        return podService.pod(internalStoreService.getStorePod(identity, store));
    }

}
