package com.asrevo.cvhome.billing.repository.projection;

import java.time.Instant;

import com.asrevo.cvhome.billing.commons.InvoiceStatus;
import com.asrevo.cvhome.billing.commons.StripeInvoiceId;
import com.asrevo.cvhome.commons.domain.CurrencyCode;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

/** One row of the platform's invoice ledger. */
@SuppressWarnings("java:S107")
public record PlatformInvoiceRow(StripeInvoiceId id, StoreMerchantId store, ManagerOrgId org, String number,
                                 InvoiceStatus status, CurrencyCode currency, Long amountDue, Long amountPaid,
                                 Instant periodStart, Instant periodEnd, Instant issuedAt, Instant paidAt,
                                 String hostedInvoiceUrl, String invoicePdfUrl) {
}
