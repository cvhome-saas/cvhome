package com.asrevo.cvhome.billing.service.stripe;

/**
 * The field names of Stripe's wire format that this service reads.
 *
 * <p>
 * Collected in one place because they are Stripe's vocabulary rather than ours, and because reading them from raw
 * JSON means the compiler cannot check a typo. Anything named here has been seen in a real payload; if a field moves
 * between API versions, this is the list to reconcile against Stripe's changelog.
 * </p>
 */
public final class StripeFields {

    public static final String ID = "id";

    public static final String STATUS = "status";

    public static final String CUSTOMER = "customer";

    public static final String SUBSCRIPTION = "subscription";

    public static final String SCHEDULE = "schedule";

    public static final String METADATA = "metadata";

    public static final String CLIENT_REFERENCE_ID = "client_reference_id";

    public static final String ITEMS = "items";

    public static final String LINES = "lines";

    public static final String PRICE = "price";

    public static final String PERIOD = "period";

    public static final String START = "start";

    public static final String END = "end";

    public static final String CURRENT_PERIOD_START = "current_period_start";

    public static final String CURRENT_PERIOD_END = "current_period_end";

    public static final String PERIOD_START = "period_start";

    public static final String PERIOD_END = "period_end";

    public static final String TRIAL_END = "trial_end";

    public static final String CANCEL_AT_PERIOD_END = "cancel_at_period_end";

    public static final String NUMBER = "number";

    public static final String CURRENCY = "currency";

    public static final String AMOUNT_DUE = "amount_due";

    public static final String AMOUNT_PAID = "amount_paid";

    public static final String HOSTED_INVOICE_URL = "hosted_invoice_url";

    public static final String INVOICE_PDF = "invoice_pdf";

    public static final String CREATED = "created";

    /**
     * Where an invoice's subscription moved to in Stripe's 2025 API versions.
     */
    public static final String PARENT = "parent";

    public static final String SUBSCRIPTION_DETAILS = "subscription_details";

    private StripeFields() {
    }

}
