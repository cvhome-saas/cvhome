package com.asrevo.cvhome.billing.api.v1;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.billing.commons.dto.ProvisionSubscriptionRequest;
import com.asrevo.cvhome.billing.commons.dto.StoreQuotaDecision;
import com.asrevo.cvhome.billing.commons.dto.StoreQuotaRequest;
import com.asrevo.cvhome.billing.commons.dto.SubscriptionView;
import com.asrevo.cvhome.billing.commons.errors.PlanNotFoundException;
import com.asrevo.cvhome.billing.service.StoreQuotaService;
import com.asrevo.cvhome.billing.services.quota.IStoreQuotaService;

import lombok.RequiredArgsConstructor;

/**
 * The store-provisioning API, called by tenancy and by nobody else.
 *
 * <p>
 * Implements {@link IStoreQuotaService} — the server-vocabulary half of the contract — so the paths and shapes here
 * and the client proxy cannot drift apart silently. Its {@code throws} clauses are this service's own exceptions;
 * callers get the rebuilt caller-side types through {@code BillingApiErrors.CATALOG}.
 * </p>
 *
 * <p>
 * Gated on {@code QUOTA-CHECK}, which only a store-core service principal holds: no human calls this, which is also
 * why these are the only billing endpoints with an org rather than a store in the request.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/quota")
@RequiredArgsConstructor
public class ExternalStoreQuotaApi implements IStoreQuotaService {

    private final StoreQuotaService storeQuotaService;

    /**
     * {@inheritDoc}
     *
     * <p>
     * Answers with a decision rather than refusing with an error, because the caller has to render <em>why</em> — and
     * whether a trial is still on the table changes what the next screen says.
     * </p>
     */
    @Override
    @PostMapping("private/store-create")
    @PreAuthorize("hasPermission(#request.org(),'ManagerOrgId','STORE-CORE.BILLING.QUOTA-CHECK')")
    public StoreQuotaDecision checkStoreCreate(@RequestBody StoreQuotaRequest request) {
        return storeQuotaService.checkStoreCreate(request.org());
    }

    @Override
    @PostMapping("private/provision")
    @PreAuthorize("hasPermission(#request.org(),'ManagerOrgId','STORE-CORE.BILLING.QUOTA-CHECK')")
    public SubscriptionView provision(@RequestBody ProvisionSubscriptionRequest request) throws PlanNotFoundException {
        return storeQuotaService.provision(request.org(), request.store());
    }

}
