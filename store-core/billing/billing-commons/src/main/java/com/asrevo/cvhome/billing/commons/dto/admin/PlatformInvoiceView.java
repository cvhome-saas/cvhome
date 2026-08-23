package com.asrevo.cvhome.billing.commons.dto.admin;

import java.io.Serializable;
import java.time.Instant;

import com.asrevo.cvhome.billing.commons.InvoiceStatus;
import com.asrevo.cvhome.billing.commons.Money;
import com.asrevo.cvhome.billing.commons.StripeInvoiceId;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

/**
 * One invoice as the platform ledger lists it: {@code InvoiceView} plus the store and org it belongs to, which one
 * store's own history does not need to say.
 */
public record PlatformInvoiceView(StripeInvoiceId id, StoreMerchantId store, ManagerOrgId org, String number,
                                  InvoiceStatus status, Money amountDue, Money amountPaid, Instant periodStart,
                                  Instant periodEnd, Instant issuedAt, Instant paidAt, String hostedInvoiceUrl,
                                  String invoicePdfUrl) implements Serializable {
}
