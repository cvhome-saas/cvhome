package com.asrevo.cvhome.billing.commons.dto;

import java.io.Serializable;
import java.time.Instant;

import com.asrevo.cvhome.billing.commons.InvoiceStatus;
import com.asrevo.cvhome.billing.commons.Money;
import com.asrevo.cvhome.billing.commons.StripeInvoiceId;

/**
 * One line of billing history.
 *
 * <p>
 * The hosted and PDF URLs are the provider's own, and are what a customer actually wants — re-rendering an invoice
 * ourselves would produce a second document disagreeing with the one their accountant already has.
 * </p>
 *
 * @param id               the provider's invoice id
 * @param number           the human-facing invoice number
 * @param status           where it stands
 * @param amountDue        what was billed
 * @param amountPaid       what was collected
 * @param periodStart      start of the period it covers
 * @param periodEnd        end of the period it covers
 * @param issuedAt         when it was issued
 * @param paidAt           when it settled, if it did
 * @param hostedInvoiceUrl the provider-hosted copy
 * @param invoicePdfUrl    the provider-hosted PDF
 */
public record InvoiceView(StripeInvoiceId id, String number, InvoiceStatus status, Money amountDue, Money amountPaid,
                          Instant periodStart, Instant periodEnd, Instant issuedAt, Instant paidAt,
                          String hostedInvoiceUrl, String invoicePdfUrl) implements Serializable {
}
