package com.asrevo.cvhome.billing.services.entitlement;

import java.util.List;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import com.asrevo.cvhome.billing.api.errors.BillingApiUnavailableException;
import com.asrevo.cvhome.billing.commons.dto.EntitlementSnapshot;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;

/**
 * What a servlet caller of billing's entitlement API depends on — the pods, and tenancy.
 *
 * <p>
 * Nothing implements this; the proxy is generated from it. Naming the caller-side exception here is what makes it
 * arrive as itself rather than wrapped.
 * </p>
 *
 * <p>
 * Callers are expected to cache what they read and to keep working when this fails. An outage in billing must not
 * stop a paying merchant trading — see {@link EntitlementSnapshot#degradedOpen}.
 * </p>
 */
@HttpExchange("/api/v1/entitlement/private")
public interface ExternalEntitlementService {

    /**
     * @throws BillingApiUnavailableException billing could not be reached; the caller should fall back rather than
     *                                        deny
     */
    @GetExchange("/snapshot")
    EntitlementSnapshot snapshot(@RequestParam("store") ManagerStoreId store) throws BillingApiUnavailableException;

    /**
     * @throws BillingApiUnavailableException billing could not be reached
     */
    @PostExchange("/snapshot/batch")
    List<EntitlementSnapshot> snapshots(@RequestBody List<ManagerStoreId> stores)
            throws BillingApiUnavailableException;

}
