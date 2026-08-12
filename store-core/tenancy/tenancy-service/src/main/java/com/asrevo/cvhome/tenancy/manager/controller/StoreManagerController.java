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
import com.asrevo.cvhome.tenancy.errors.DuplicateStoreNameException;
import com.asrevo.cvhome.tenancy.errors.StoreNotFoundException;
import com.asrevo.cvhome.tenancy.errors.StoreNotOperableException;
import com.asrevo.cvhome.tenancy.manager.service.InternalStoreService;
import com.asrevo.cvhome.tenancy.manager.service.StoreManagerService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("api/v1/store-manager")
@AllArgsConstructor
@Slf4j
public class StoreManagerController {

    /**
     * Who may ask for a store listing at all. These endpoints scope their rows by the caller's org in the query, so
     * this is a coarse gate rather than the isolation boundary — but without it any authenticated principal could
     * ask, and a principal holding none of these roles used to receive every store on the platform.
     *
     * <p>
     * An inline role list rather than a {@code hasPermission} token because there is no single store to evaluate one
     * against; the store-scoped endpoints below use tokens.
     * </p>
     */
    private static final String STORE_VIEWER_ROLES = """
            hasAnyRole('ROLE_SUPER_ADMIN','ROLE_ORG_ADMIN','ROLE_STORE_ADMIN','ROLE_STORE_MODERATOR','ROLE_STORE_RETAIL')""";

    private final StoreManagerService managerService;

    private final InternalStoreService internalStoreService;

    /**
     * Rows are confined to the caller's org inside {@link InternalStoreService#findAll}, so this guard is about who
     * may ask at all, not which stores come back.
     */
    @PostMapping("list")
    @PreAuthorize(STORE_VIEWER_ROLES)
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
            PodRegistryUnavailableException, DuplicateStoreNameException {
        return this.managerService.createStore(identity.org(), request);
    }

    /**
     * Store names are unique platform-wide, so this necessarily reports on names outside the caller's org — it is the
     * pre-flight check for the create form. Restricted to those who can actually create a store, so it cannot be used
     * to enumerate other tenants' store names.
     */
    @GetMapping(value = "private/store/unique", params = "name")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN','ROLE_ORG_ADMIN')")
    public Map<String, Boolean> checkExist(@RequestParam("name") String name) {
        return Map.of("exists", internalStoreService.checkNameExists(name));
    }

    /** Same listing as {@link #findAllStores}, without a filter body. The guard was commented out. */
    @GetMapping("private/store")
    @PreAuthorize(STORE_VIEWER_ROLES)
    public Page<ManagerStoreDto> findAllStoresDetailed(@OrgStorePrincipalInfo UserOrgStoreIdentity identity,
                                                       Pageable pageable) {
        return internalStoreService.findAll(identity, new ListManagerStoreQuery(null, null, null), pageable);
    }

    @GetMapping("private/store/{code}")
    @PreAuthorize("hasPermission(#store,'ManagerStoreId','STORE-CORE.STORE-FIND-ONE')")
    public Object getStoreDetailed(@OrgStorePrincipalInfo UserOrgStoreIdentity identity,
                                   @PathVariable("code") ManagerStoreId store)
            throws StoreNotFoundException, StoreNotOperableException {
        return managerService.getStore(identity, store);
    }

    @GetMapping("store-info")
    @PreAuthorize("hasPermission(#store,'ManagerStoreId','STORE-CORE.STORE-FIND-ONE')")
    public ManagerStoreDto storeInfo(@OrgStorePrincipalInfo UserOrgStoreIdentity identity,
                                     @RequestParam ManagerStoreId store) throws StoreNotFoundException {
        return internalStoreService.findStore(identity, store);
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
