package com.asrevo.cvhome.tenancy.manager.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.commons.annotation.OrgStorePrincipalInfo;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;
import com.asrevo.cvhome.podregistry.commons.errors.PodNotFoundException;
import com.asrevo.cvhome.podregistry.services.pod.CachingPodDirectory;
import com.asrevo.cvhome.tenancy.errors.StoreNotFoundException;
import com.asrevo.cvhome.tenancy.errors.StoreNotOperableException;
import com.asrevo.cvhome.tenancy.manager.service.InternalStoreService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("api/v1/router")
@AllArgsConstructor
@Slf4j
public class RouterController {

    public final InternalStoreService internalStoreService;

    private final CachingPodDirectory podDirectory;

    /**
     * Which pod hosts a store.
     *
     * <p>
     * The two halves come from different owners now: tenancy holds the store→pod binding in
     * {@code manager_store.pod_id}, and the pod registry holds what that pod actually is. The lookup goes through
     * {@link CachingPodDirectory}, which degrades to its last known map and then to the configuration seed, so a
     * registry outage does not take this endpoint down.
     * </p>
     *
     * <p>
     * Guarded twice on purpose: the permission gate, and an org check inside
     * {@link InternalStoreService#getStorePod(UserOrgStoreIdentity, ManagerStoreId)} — the gate alone does not hold
     * a foreign store out, because the shared {@code isOrgAdmin} ignores the store it is asked about.
     * </p>
     *
     * @throws StoreNotFoundException the store does not exist, or belongs to another organization
     * @throws StoreNotOperableException the store is suspended or archived, or its organization is closed
     * @throws PodNotFoundException   the store names a pod the registry has never heard of. A real inconsistency —
     *                                the binding outlived the pod — so it is an error rather than the {@code null}
     *                                body this used to return through {@code PodRepository.orElse(null)}
     */
    @GetMapping("store-pod-by-store-id")
    @PreAuthorize("hasPermission(#store,'ManagerStoreId','STORE-CORE.STORE-FIND-ONE')")
    public Pod getStorePodByStoreId(@OrgStorePrincipalInfo UserOrgStoreIdentity identity,
                                    @RequestParam ManagerStoreId store)
            throws StoreNotFoundException, PodNotFoundException, StoreNotOperableException {
        // Refused for a suspended or archived store, and for one whose organization is closed: this is the
        // call the console makes to enter a store, so it is where "suspended" has to bite. Reading the
        // store's own record stays allowed, or the console could not show why it is closed.
        internalStoreService.requireOperable(store);
        PodId podId = internalStoreService.getStorePod(identity, store);
        return podDirectory.find(podId).orElseThrow(() -> {
            log.error("Store {} is bound to pod {}, which the registry does not know", store, podId);
            return PodNotFoundException.of(podId);
        });
    }

}
