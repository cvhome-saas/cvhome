package com.asrevo.cvhome.billing.commons.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.asrevo.cvhome.billing.commons.EntitlementKey;
import com.asrevo.cvhome.billing.commons.EntitlementValue;
import com.asrevo.cvhome.billing.commons.PlanId;

/**
 * One plan in the catalog, with everything a pricing page needs.
 *
 * @param id           the catalog id
 * @param code         the stable handle, e.g. {@code PRO}
 * @param displayName  what to show
 * @param description  the marketing line, may be null
 * @param tier         ordering; a move to a higher tier is an upgrade, a lower one a downgrade
 * @param prices       purchasable prices, filtered to the requested currency where one was given
 * @param entitlements what the plan grants
 */
public record PlanView(PlanId id, String code, String displayName, String description, Integer tier,
                       List<PlanPriceView> prices, Map<EntitlementKey, EntitlementValue> entitlements)
        implements Serializable {
}
