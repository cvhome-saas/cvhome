package com.asrevo.cvhome.billing.domain;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.asrevo.cvhome.billing.commons.InvoiceStatus;
import com.asrevo.cvhome.billing.commons.Money;
import com.asrevo.cvhome.billing.commons.StripeInvoiceId;
import com.asrevo.cvhome.billing.commons.StripeSubscriptionId;
import com.asrevo.cvhome.commons.domain.CurrencyCode;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import lombok.Getter;

/**
 * One invoice, mirrored from Stripe so billing history can be read without calling out.
 *
 * <p>
 * Keyed by Stripe's invoice id rather than one of ours, which makes recording an invoice naturally idempotent: the
 * same invoice arriving on {@code invoice.payment_succeeded} and again on a redelivery updates one row.
 * </p>
 *
 * <p>
 * The hosted and PDF URLs are Stripe's own and are what customers are shown. Re-rendering an invoice ourselves would
 * produce a second document that could disagree with the one their accountant already has.
 * </p>
 */
@Getter
@Table(schema = "billing", name = "subscription_invoice")
public class SubscriptionInvoiceEntity {

    @Id
    @Column("id")
    private StripeInvoiceId id;

    @Column("store_id")
    private StoreMerchantId storeId;

    @Column("org_id")
    private ManagerOrgId orgId;

    @Column("stripe_subscription_id")
    private StripeSubscriptionId stripeSubscriptionId;

    @Column("invoice_number")
    private String invoiceNumber;

    @Column("status")
    private InvoiceStatus status;

    @Column("currency")
    private CurrencyCode currency;

    @Column("amount_due")
    private Long amountDue;

    @Column("amount_paid")
    private Long amountPaid;

    @Column("period_start")
    private Instant periodStart;

    @Column("period_end")
    private Instant periodEnd;

    @Column("hosted_invoice_url")
    private String hostedInvoiceUrl;

    @Column("invoice_pdf_url")
    private String invoicePdfUrl;

    @Column("issued_at")
    private Instant issuedAt;

    @Column("paid_at")
    private Instant paidAt;

    @Column("created_date")
    private Instant createdDate;

    @Version
    private Integer version;

    @SuppressWarnings("java:S107")
    public static SubscriptionInvoiceEntity record(StripeInvoiceId id, StoreMerchantId store, ManagerOrgId org,
                                                   StripeSubscriptionId subscription, String number,
                                                   InvoiceStatus status, Money amountDue, Long amountPaid,
                                                   Instant issuedAt) {
        SubscriptionInvoiceEntity entity = new SubscriptionInvoiceEntity();
        entity.id = id;
        entity.storeId = store;
        entity.orgId = org;
        entity.stripeSubscriptionId = subscription;
        entity.invoiceNumber = number;
        entity.status = status;
        entity.currency = amountDue.currency();
        entity.amountDue = amountDue.minorUnits();
        entity.amountPaid = amountPaid;
        entity.issuedAt = issuedAt;
        entity.createdDate = Instant.now();
        return entity;
    }

    public SubscriptionInvoiceEntity covering(Instant start, Instant end) {
        this.periodStart = start;
        this.periodEnd = end;
        return this;
    }

    public SubscriptionInvoiceEntity hostedAt(String hostedUrl, String pdfUrl) {
        this.hostedInvoiceUrl = hostedUrl;
        this.invoicePdfUrl = pdfUrl;
        return this;
    }

    public SubscriptionInvoiceEntity settled(InvoiceStatus newStatus, Long paid, Instant at) {
        this.status = newStatus;
        this.amountPaid = paid;
        this.paidAt = at;
        return this;
    }

    public Money amountDue() {
        return new Money(currency, amountDue);
    }

    public Money amountPaid() {
        return new Money(currency, amountPaid);
    }

}
