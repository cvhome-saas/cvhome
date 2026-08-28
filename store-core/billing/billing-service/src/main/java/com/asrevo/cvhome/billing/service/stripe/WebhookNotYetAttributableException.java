package com.asrevo.cvhome.billing.service.stripe;

import java.io.Serial;

/**
 * A webhook that belongs to a customer we know, about a subscription we have not bound yet.
 *
 * <p>
 * Stripe does not promise delivery order, and an invoice names only its subscription — so
 * {@code invoice.payment_succeeded} can arrive before the {@code customer.subscription.created} that tells us which
 * store that subscription is. Recording such an event as ignored would drop a paid invoice from billing history
 * permanently, since the id would then be known and never reprocessed.
 * </p>
 *
 * <p>
 * So it is deliberately <em>not</em> recorded, and the request fails: Stripe redelivers with backoff, and by then the
 * binding has landed. Unchecked because it escapes through the controller to become a 5xx, which is precisely the
 * signal that asks Stripe to try again.
 * </p>
 *
 * <p>
 * Narrow on purpose. This only applies when the invoice's customer is one of ours; an invoice for a customer we have
 * never seen is genuinely foreign and is ignored rather than retried for days.
 * </p>
 */
public class WebhookNotYetAttributableException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public WebhookNotYetAttributableException(String eventId, String eventType) {
        super(String.format("Stripe event %s of type %s names a subscription that is not bound to a store yet",
                eventId, eventType));
    }

}
