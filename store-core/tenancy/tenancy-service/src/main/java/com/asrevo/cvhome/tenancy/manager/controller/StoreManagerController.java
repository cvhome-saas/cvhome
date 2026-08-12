package com.asrevo.cvhome.tenancy.manager.controller;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.billing.api.errors.BillingApiUnavailableException;
import com.asrevo.cvhome.billing.api.errors.StoreQuotaRefusedException;
import com.asrevo.cvhome.commons.annotation.OrgStorePrincipalInfo;
import com.asrevo.cvhome.commons.domain.ColorTheme;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.SocialProvider;
import com.asrevo.cvhome.commons.domain.Theme;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;
import com.asrevo.cvhome.podregistry.api.errors.PodPlacementRefusedException;
import com.asrevo.cvhome.podregistry.api.errors.PodRegistryUnavailableException;
import com.asrevo.cvhome.tenancy.commons.dto.ListManagerStoreQuery;
import com.asrevo.cvhome.tenancy.commons.dto.ManagerStoreDto;
import com.asrevo.cvhome.tenancy.manager.service.InternalStoreService;
import com.asrevo.cvhome.tenancy.manager.service.StoreManagerService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("api/v1/store-manager")
@AllArgsConstructor
@Slf4j
public class StoreManagerController {

    private final StoreManagerService managerService;

    private final InternalStoreService internalStoreService;

    @PostMapping("list")

    public Page<ManagerStoreDto> findAllStores(@OrgStorePrincipalInfo UserOrgStoreIdentity identity,
                                               @RequestBody ListManagerStoreQuery listManagerStoreQuery, Pageable pageable) {
        return internalStoreService.findAll(identity, listManagerStoreQuery, pageable);
    }

    /**
     * @throws StoreQuotaRefusedException      billing will not let this org have another store — 422 with the reason
     * @throws BillingApiUnavailableException  billing could not be reached, so the store is not created; the caller
     *                                         should retry rather than assume it exists
     * @throws PodPlacementRefusedException    the registry has nowhere to put it — 422. An operational fault, not
     *                                         something the merchant can resolve: someone has to drain, resize or
     *                                         add a pod
     * @throws PodRegistryUnavailableException the registry could not be reached, so the store is not created
     */
    @PostMapping("private/store")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN','ROLE_ORG_ADMIN')")
    public ManagerStoreDto create(@OrgStorePrincipalInfo UserOrgStoreIdentity identity,
                                  @RequestBody Map<Object, Object> request)
            throws StoreQuotaRefusedException, BillingApiUnavailableException, PodPlacementRefusedException,
            PodRegistryUnavailableException {
        return this.managerService.createStore(identity.org(), request);
    }

    @GetMapping(value = "private/store/unique", params = "name")

    public Map<String, Boolean> checkExist(@RequestParam("name") String name) {
        return Map.of("exists", internalStoreService.checkNameExists(name));
    }

    @GetMapping("private/store")
    // @PreAuthorize("hasAnyRole('ROLE_ORG_ADMIN')")

    public Page<ManagerStoreDto> findAllStoresDetailed(@OrgStorePrincipalInfo UserOrgStoreIdentity identity,
                                                       Pageable pageable) {
        return internalStoreService.findAll(identity, new ListManagerStoreQuery(null, null, null), pageable);
    }

    @GetMapping("private/store/{code}")
    @PreAuthorize("hasPermission(#store,'ManagerStoreId','STORE-CORE.STORE-FIND-ONE')")
    public Object getStoreDetailed(@OrgStorePrincipalInfo UserOrgStoreIdentity identity,
                                   @PathVariable("code") ManagerStoreId store) {
        return managerService.getStore(store);
    }

    @GetMapping("store-info")
    @PreAuthorize("hasPermission(#store,'ManagerStoreId','STORE-CORE.STORE-FIND-ONE')")
    public ManagerStoreDto storeInfo(@OrgStorePrincipalInfo UserOrgStoreIdentity identity,
                                     @RequestParam ManagerStoreId store) {
        return internalStoreService.findStore(store);
    }

    @GetMapping("public/themes")
    public List<Theme> themes() {
        return Theme.getImplementedThemes();
    }

    @GetMapping("public/color-themes")
    public ColorTheme[] colorThemes() {
        return ColorTheme.values();
    }

    @GetMapping("public/social-links-providers")
    public SocialProvider[] socialLinkProviders() {
        return SocialProvider.values();
    }

}
