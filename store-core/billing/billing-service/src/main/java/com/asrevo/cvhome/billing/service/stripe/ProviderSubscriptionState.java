package com.asrevo.cvhome.billing.service.stripe;

import java.time.Instant;

import com.google.gson.JsonObject;

/**
 * What Stripe says a subscription looks like right now, read out of a webhook payload.
 *
 * <p>
 * A narrow projection of Stripe's object: only the fields that drive a local transition. Keeping it explicit means a
 * handler cannot quietly start depending on some other field of the payload without that appearing here first.
 * </p>
 *
 * @param subscriptionId    Stripe's subscription id
 * @param customerId        Stripe's customer id
 * @param status            Stripe's own status string, e.g. {@code active}, {@code past_due}, {@code trialing}
 * @param priceId           the Stripe price the subscription is currently on
 * @param scheduleId        the subscription schedule backing a deferred change, if any
 * @param currentPeriodStart start of the period Stripe considers open
 * @param currentPeriodEnd   end of that period — the next renewal date
 * @param trialEnd          when Stripe's own trial ends, if it is in one
 * @param cancelAtPeriodEnd whether renewal has been switched off
 */
public record ProviderSubscriptionState(String subscriptionId, String customerId, String status, String priceId,
                                        String scheduleId, Instant currentPeriodStart, Instant currentPeriodEnd,
                                        Instant trialEnd, boolean cancelAtPeriodEnd) {

    /**
     * Reads a Stripe subscription object.
     *
     * <p>
     * The period window is looked for on the subscription and then on its first item. Stripe moved those fields onto
     * items in its 2025 API versions, and an account can be pinned to either — reading both is what lets one build
     * serve accounts on both versions instead of silently recording a null renewal date.
     * </p>
     */
    public static ProviderSubscriptionState from(JsonObject subscription) {
        JsonObject item = StripeJson.firstOfData(subscription, StripeFields.ITEMS);
        Instant periodStart = firstNonNull(StripeJson.timestamp(subscription, StripeFields.CURRENT_PERIOD_START),
                StripeJson.timestamp(item, StripeFields.CURRENT_PERIOD_START));
        Instant periodEnd = firstNonNull(StripeJson.timestamp(subscription, StripeFields.CURRENT_PERIOD_END),
                StripeJson.timestamp(item, StripeFields.CURRENT_PERIOD_END));
        return new ProviderSubscriptionState(
                StripeJson.string(subscription, StripeFields.ID),
                StripeJson.string(subscription, StripeFields.CUSTOMER),
                StripeJson.string(subscription, StripeFields.STATUS),
                StripeJson.string(StripeJson.object(item, StripeFields.PRICE), StripeFields.ID),
                StripeJson.string(subscription, StripeFields.SCHEDULE),
                periodStart,
                periodEnd,
                StripeJson.timestamp(subscription, StripeFields.TRIAL_END),
                StripeJson.flag(subscription, StripeFields.CANCEL_AT_PERIOD_END));
    }

    private static Instant firstNonNull(Instant preferred, Instant fallback) {
        return preferred != null ? preferred : fallback;
    }

    /**
     * Whether Stripe considers this subscription to be collecting money successfully.
     */
    public boolean paying() {
        return "active".equals(status) || "trialing".equals(status);
    }

    public boolean pastDue() {
        return "past_due".equals(status) || "unpaid".equals(status);
    }

    public boolean ended() {
        return "canceled".equals(status) || "incomplete_expired".equals(status);
    }

}
