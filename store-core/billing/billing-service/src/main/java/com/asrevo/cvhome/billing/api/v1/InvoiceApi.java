package com.asrevo.cvhome.billing.api.v1;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.billing.commons.dto.InvoiceView;
import com.asrevo.cvhome.billing.service.InvoiceService;
import com.asrevo.cvhome.commons.annotation.OrgStorePrincipalInfo;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;

import lombok.RequiredArgsConstructor;

/**
 * A store's billing history.
 *
 * <p>
 * Read-gated rather than manage-gated: seeing what was charged is part of working in a store, while changing what
 * will be charged is not. The rows carry Stripe's own hosted and PDF links, which are what a customer's accountant
 * should be given — a document we rendered ourselves could disagree with the one Stripe issued.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/invoice")
@RequiredArgsConstructor
public class InvoiceApi {

    private final InvoiceService invoiceService;

    @GetMapping("list")
    @PreAuthorize("hasPermission(#store,'StoreMerchantId','STORE-CORE.BILLING.READ')")
    public Page<InvoiceView> list(@OrgStorePrincipalInfo UserOrgStoreIdentity identity,
                                  @RequestParam("store") StoreMerchantId store, Pageable pageable) {
        return invoiceService.list(store, tenantScopeOf(identity), pageable);
    }

    /**
     * The org this listing is confined to — the same tenant guard as {@code SubscriptionApi}, and for the same
     * reason: the shared permission checker cannot tell which org a store belongs to, so the boundary lives in the
     * query.
     */
    private ManagerOrgId tenantScopeOf(UserOrgStoreIdentity identity) {
        return identity.isSuperAdmin() ? null : identity.org();
    }

}
