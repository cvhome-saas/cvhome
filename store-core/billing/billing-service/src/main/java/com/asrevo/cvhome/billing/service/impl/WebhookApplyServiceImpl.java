package com.asrevo.cvhome.billing.service.impl;

import java.time.Instant;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.billing.commons.AuditEventType;
import com.asrevo.cvhome.billing.commons.InvoiceStatus;
import com.asrevo.cvhome.billing.commons.Money;
import com.asrevo.cvhome.billing.commons.StripeCustomerId;
import com.asrevo.cvhome.billing.commons.StripeEventId;
import com.asrevo.cvhome.billing.commons.StripeInvoiceId;
import com.asrevo.cvhome.billing.commons.StripeScheduleId;
import com.asrevo.cvhome.billing.commons.StripeSubscriptionId;
import com.asrevo.cvhome.billing.commons.SubscriptionStatus;
import com.asrevo.cvhome.billing.commons.errors.IllegalSubscriptionTransitionException;
import com.asrevo.cvhome.billing.commons.errors.PlanPriceNotFoundException;
import com.asrevo.cvhome.billing.commons.errors.SubscriptionNotFoundException;
import com.asrevo.cvhome.billing.config.BillingProperties;
import com.asrevo.cvhome.billing.domain.PlanPriceEntity;
import com.asrevo.cvhome.billing.domain.StoreSubscriptionEntity;
import com.asrevo.cvhome.billing.domain.SubscriptionInvoiceEntity;
import com.asrevo.cvhome.billing.repository.StoreSubscriptionRepository;
import com.asrevo.cvhome.billing.repository.SubscriptionInvoiceRepository;
import com.asrevo.cvhome.billing.service.PlanCatalogService;
import com.asrevo.cvhome.billing.service.SubscriptionAuditService;
import com.asrevo.cvhome.billing.service.WebhookApplyService;
import com.asrevo.cvhome.billing.service.stripe.ProviderSubscriptionState;
import com.asrevo.cvhome.billing.service.stripe.StripeFields;
import com.asrevo.cvhome.billing.service.stripe.StripeJson;
import com.asrevo.cvhome.commons.domain.CurrencyCode;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.google.gson.JsonObject;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookApplyServiceImpl implements WebhookApplyService {

    private final StoreSubscriptionRepository subscriptionRepository;

    private final SubscriptionInvoiceRepository invoiceRepository;

    private final PlanCatalogService planCatalogService;

    private final SubscriptionAuditService auditService;

    private final BillingProperties properties;

    @Override
    @Transactional
    public void applyCheckoutCompleted(ManagerStoreId store, String eventId, String payload)
            throws SubscriptionNotFoundException {
        JsonObject session = StripeJson.parse(payload);
        String customer = StripeJson.string(session, StripeFields.CUSTOMER);
        String subscription = StripeJson.string(session, StripeFields.SUBSCRIPTION);
        if (customer == null || subscription == null) {
            log.warn("Checkout session for store {} carried no customer or subscription — nothing to bind", store);
            return;
        }
        StoreSubscriptionEntity entity = require(store);
        subscriptionRepository.save(entity.bindProvider(new StripeCustomerId(customer),
                new StripeSubscriptionId(subscription)));
        log.info("Bound store {} to Stripe subscription {}", store, subscription);
    }

    @Override
    @Transactional
    public void applySubscriptionChanged(ManagerStoreId store, String eventId, String payload)
            throws SubscriptionNotFoundException, IllegalSubscriptionTransitionException, PlanPriceNotFoundException {
        ProviderSubscriptionState state = ProviderSubscriptionState.from(StripeJson.parse(payload));
        StoreSubscriptionEntity entity = require(store);
        SubscriptionStatus before = entity.getStatus();

        bindIdentifiers(entity, state);
        applyPlanOf(entity, state);
        applyStatusOf(entity, state);

        StoreSubscriptionEntity saved = subscriptionRepository.save(entity);
        if (saved.getStatus() != before) {
            auditService.recordFromWebhook(before, saved, auditTypeFor(saved.getStatus()),
                    new StripeEventId(eventId));
        }
        log.info("Reconciled store {} with Stripe: {} -> {}", store, before, saved.getStatus());
    }

    @Override
    @Transactional
    public void applySubscriptionEnded(ManagerStoreId store, String eventId, String payload)
            throws SubscriptionNotFoundException, IllegalSubscriptionTransitionException {
        StoreSubscriptionEntity entity = require(store);
        SubscriptionStatus before = entity.getStatus();
        if (before == SubscriptionStatus.CANCELED) {
            return;
        }
        StoreSubscriptionEntity saved = subscriptionRepository.save(entity.cancelNow(Instant.now()));
        auditService.recordFromWebhook(before, saved, AuditEventType.CANCELED, new StripeEventId(eventId));
        log.info("Store {} cancelled by Stripe (was {})", store, before);
    }

    @Override
    @Transactional
    public void applyInvoicePaid(ManagerStoreId store, String eventId, String payload)
            throws SubscriptionNotFoundException, IllegalSubscriptionTransitionException, PlanPriceNotFoundException {
        JsonObject invoice = StripeJson.parse(payload);
        StoreSubscriptionEntity entity = require(store);
        SubscriptionStatus before = entity.getStatus();

        recordInvoice(entity, invoice, InvoiceStatus.PAID);

        Instant periodStart = periodStartOf(invoice);
        Instant periodEnd = periodEndOf(invoice);
        PlanPriceEntity price = priceOf(invoice, entity);
        StoreSubscriptionEntity saved = subscriptionRepository.save(
                entity.activate(price.getPlanId(), price.getId(), periodStart, periodEnd));
        auditService.recordFromWebhook(before, saved,
                before == SubscriptionStatus.ACTIVE ? AuditEventType.RENEWED : AuditEventType.ACTIVATED,
                new StripeEventId(eventId));
        log.info("Store {} paid: {} -> {}, next renewal {}", store, before, saved.getStatus(), periodEnd);
    }

    @Override
    @Transactional
    public void applyInvoiceFailed(ManagerStoreId store, String eventId, String payload)
            throws SubscriptionNotFoundException, IllegalSubscriptionTransitionException {
        JsonObject invoice = StripeJson.parse(payload);
        StoreSubscriptionEntity entity = require(store);
        SubscriptionStatus before = entity.getStatus();

        recordInvoice(entity, invoice, InvoiceStatus.OPEN);

        Instant graceUntil = Instant.now().plus(properties.pastDueGrace());
        StoreSubscriptionEntity saved = subscriptionRepository.save(entity.markPastDue(graceUntil));
        auditService.recordFromWebhook(before, saved, AuditEventType.PAYMENT_FAILED, new StripeEventId(eventId));
        log.info("Store {} failed a renewal: {} -> {}, grace until {}", store, before, saved.getStatus(), graceUntil);
    }

    /**
     * Records or updates the invoice row.
     *
     * <p>
     * Keyed by Stripe's invoice id, so the same invoice arriving twice — a redelivery, or a failure then a success —
     * updates one row instead of accumulating history that never happened.
     * </p>
     */
    private void recordInvoice(StoreSubscriptionEntity subscription, JsonObject invoice, InvoiceStatus status) {
        String id = StripeJson.string(invoice, StripeFields.ID);
        if (id == null) {
            return;
        }
        StripeInvoiceId invoiceId = new StripeInvoiceId(id);
        Long amountPaid = orZero(StripeJson.number(invoice, StripeFields.AMOUNT_PAID));
        Money amountDue = new Money(new CurrencyCode(currencyOf(invoice)),
                orZero(StripeJson.number(invoice, StripeFields.AMOUNT_DUE)));
        Instant paidAt = status == InvoiceStatus.PAID ? Instant.now() : null;

        SubscriptionInvoiceEntity entity = invoiceRepository.findById(invoiceId)
                .map(it -> it.settled(status, amountPaid, paidAt))
                .orElseGet(() -> SubscriptionInvoiceEntity.record(invoiceId, subscription.getId(),
                        subscription.getOrgId(), subscription.getStripeSubscriptionId(),
                        StripeJson.string(invoice, StripeFields.NUMBER), status, amountDue, amountPaid,
                        firstNonNull(StripeJson.timestamp(invoice, StripeFields.CREATED), Instant.now())));
        invoiceRepository.save(entity
                .covering(periodStartOf(invoice), periodEndOf(invoice))
                .hostedAt(StripeJson.string(invoice, StripeFields.HOSTED_INVOICE_URL),
                        StripeJson.string(invoice, StripeFields.INVOICE_PDF)));
    }

    private void bindIdentifiers(StoreSubscriptionEntity entity, ProviderSubscriptionState state) {
        if (state.subscriptionId() != null && entity.getStripeSubscriptionId() == null) {
            entity.bindProvider(state.customerId() == null ? entity.getStripeCustomerId()
                    : new StripeCustomerId(state.customerId()), new StripeSubscriptionId(state.subscriptionId()));
        }
        if (state.scheduleId() != null) {
            entity.bindSchedule(new StripeScheduleId(state.scheduleId()));
        }
    }

    /**
     * Moves the local plan to whatever Stripe now charges for.
     *
     * <p>
     * This is where a deferred downgrade actually lands: the schedule flips the price at the period boundary and
     * Stripe reports the new one here. The call is a no-op when nothing was pending, which is what makes the
     * safety-net job and this handler safe to both run.
     * </p>
     */
    private void applyPlanOf(StoreSubscriptionEntity entity, ProviderSubscriptionState state)
            throws PlanPriceNotFoundException {
        if (state.priceId() == null) {
            return;
        }
        PlanPriceEntity price = planCatalogService.findByStripePriceId(state.priceId())
                .orElseThrow(() -> PlanPriceNotFoundException.of(state.priceId()));
        if (price.getId().equals(entity.getPlanPriceId())) {
            return;
        }
        if (entity.getPendingPlanPriceId() != null) {
            entity.applyPendingChange(price.getPlanId(), price.getId());
        } else {
            entity.upgradeTo(price.getPlanId(), price.getId(), state.currentPeriodStart(), state.currentPeriodEnd());
        }
    }

    private void applyStatusOf(StoreSubscriptionEntity entity, ProviderSubscriptionState state)
            throws IllegalSubscriptionTransitionException {
        // Mirrored both ways: the provider decides whether this renews, so a flag that only ever went on would
        // leave a resumed subscription reading as cancelled the moment a late webhook arrived.
        entity.reconcileRenewal(state.cancelAtPeriodEnd());
        if (state.ended()) {
            entity.cancelNow(Instant.now());
            return;
        }
        if (state.pastDue()) {
            entity.markPastDue(Instant.now().plus(properties.pastDueGrace()));
            return;
        }
        if (state.paying() && state.currentPeriodEnd() != null) {
            entity.renew(state.currentPeriodStart(), state.currentPeriodEnd());
        }
    }

    /**
     * The plan the invoice was for, falling back to whatever the subscription already names.
     *
     * <p>
     * The fallback matters for the very first invoice of a trial converting to paid: the local row already knows its
     * price, and losing it would leave a paying store with no plan.
     * </p>
     */
    private PlanPriceEntity priceOf(JsonObject invoice, StoreSubscriptionEntity entity)
            throws PlanPriceNotFoundException {
        JsonObject line = StripeJson.firstOfData(invoice, StripeFields.LINES);
        String stripePriceId = StripeJson.string(StripeJson.object(line, StripeFields.PRICE), StripeFields.ID);
        if (stripePriceId != null) {
            var found = planCatalogService.findByStripePriceId(stripePriceId);
            if (found.isPresent()) {
                return found.get();
            }
        }
        if (entity.getPlanPriceId() == null) {
            throw PlanPriceNotFoundException.of(stripePriceId);
        }
        return planCatalogService.requirePrice(entity.getPlanPriceId());
    }

    /**
     * The service period an invoice paid for.
     *
     * <p>
     * The <em>line item's</em> period is the authority, not the invoice's own {@code period_start}/{@code period_end}.
     * Those describe the invoice document, and on a subscription's first invoice both are simply the moment it was
     * cut — so reading them made a monthly subscription report a renewal date of today. The line carries the real
     * window. Invoice-level values remain a fallback for the rare invoice with no lines.
     * </p>
     */
    private Instant periodStartOf(JsonObject invoice) {
        JsonObject line = StripeJson.firstOfData(invoice, StripeFields.LINES);
        Instant fromLine = StripeJson.timestamp(StripeJson.object(line, StripeFields.PERIOD), StripeFields.START);
        return fromLine != null ? fromLine : StripeJson.timestamp(invoice, StripeFields.PERIOD_START);
    }

    /**
     * When the paid period ends — the next renewal date the customer is shown. See {@link #periodStartOf} for why the
     * line item wins over the invoice's own fields.
     */
    private Instant periodEndOf(JsonObject invoice) {
        JsonObject line = StripeJson.firstOfData(invoice, StripeFields.LINES);
        Instant fromLine = StripeJson.timestamp(StripeJson.object(line, StripeFields.PERIOD), StripeFields.END);
        return fromLine != null ? fromLine : StripeJson.timestamp(invoice, StripeFields.PERIOD_END);
    }

    private AuditEventType auditTypeFor(SubscriptionStatus status) {
        return switch (status) {
            case ACTIVE -> AuditEventType.ACTIVATED;
            case PAST_DUE -> AuditEventType.PAST_DUE;
            case SUSPENDED -> AuditEventType.SUSPENDED;
            case CANCELED -> AuditEventType.CANCELED;
            case TRIALING -> AuditEventType.TRIAL_STARTED;
            case PENDING -> AuditEventType.CREATED;
        };
    }

    private String currencyOf(JsonObject invoice) {
        String currency = StripeJson.string(invoice, StripeFields.CURRENCY);
        return currency == null ? "usd" : currency.toUpperCase(Locale.ROOT);
    }

    private Long orZero(Long value) {
        return value == null ? 0L : value;
    }

    private Instant firstNonNull(Instant preferred, Instant fallback) {
        return preferred != null ? preferred : fallback;
    }

    private StoreSubscriptionEntity require(ManagerStoreId store) throws SubscriptionNotFoundException {
        return subscriptionRepository.findById(store)
                .orElseThrow(() -> SubscriptionNotFoundException.forStore(store));
    }

}
