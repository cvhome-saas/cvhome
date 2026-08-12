package com.asrevo.cvhome.billing.api.v1;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.billing.commons.dto.EntitlementSnapshot;
import com.asrevo.cvhome.billing.commons.errors.SubscriptionNotFoundException;
import com.asrevo.cvhome.billing.service.EntitlementService;
import com.asrevo.cvhome.billing.services.entitlement.IEntitlementService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import lombok.RequiredArgsConstructor;

/**
 * What a store is allowed to do, for the services that enforce it.
 *
 * <p>
 * Gated on {@code ENTITLEMENT-READ}, which is wider than the other billing tokens on purpose: the pods enforce these
 * ceilings, so a store-pod service principal has to be able to ask, not only a human.
 * </p>
 *
 * <p>
 * Every method here is a read, and every caller is expected to keep working when it fails. That asymmetry with store
 * creation — which fails closed — is deliberate: refusing to create one store is recoverable, while denying every
 * write on the platform because billing is down is not.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/entitlement")
@RequiredArgsConstructor
public class ExternalEntitlementApi implements IEntitlementService {

    private final EntitlementService entitlementService;

    @Override
    @GetMapping("private/snapshot")
    @PreAuthorize("hasPermission(#store,'StoreMerchantId','STORE-CORE.BILLING.ENTITLEMENT-READ')")
    public EntitlementSnapshot snapshot(@RequestParam("store") StoreMerchantId store)
            throws SubscriptionNotFoundException {
        return entitlementService.snapshot(store);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * Authorised on the store-pod or store-core scope rather than per store in the body: the callers are services
     * rendering or guarding many stores at once, and checking each id would turn one call back into the N this
     * endpoint exists to avoid. A human session reaches it only for stores it could read individually anyway,
     * because the snapshots returned carry nothing a store's own {@code current} would not.
     * </p>
     */
    @Override
    @PostMapping("private/snapshot/batch")
    @PreAuthorize("hasPermission(null,'StoreMerchantId','STORE-CORE.BILLING.QUOTA-CHECK')")
    public List<EntitlementSnapshot> snapshots(@RequestBody List<StoreMerchantId> stores) {
        return entitlementService.snapshots(stores);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The gateway polls this, so it is scoped to a service principal rather than a store.
     * </p>
     */
    @Override
    @GetMapping("private/blocked-stores")
    @PreAuthorize("hasPermission(null,'StoreMerchantId','STORE-CORE.BILLING.QUOTA-CHECK')")
    public List<StoreMerchantId> blockedStores() {
        return entitlementService.blockedStores();
    }

}
