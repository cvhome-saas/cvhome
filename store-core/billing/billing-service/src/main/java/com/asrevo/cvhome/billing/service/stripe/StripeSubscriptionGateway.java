package com.asrevo.cvhome.billing.service.stripe;

import java.time.Instant;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.asrevo.cvhome.billing.commons.StripeScheduleId;
import com.asrevo.cvhome.billing.commons.StripeSubscriptionId;
import com.asrevo.cvhome.billing.commons.errors.BillingProviderUnavailableException;
import com.asrevo.cvhome.billing.commons.errors.SubscriptionChangeRejectedException;
import com.asrevo.cvhome.billing.config.StripeCredentials;
import com.asrevo.cvhome.billing.domain.PlanPriceEntity;
import com.asrevo.cvhome.billing.repository.StripeRequestRepository;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.stripe.StripeClient;
import com.stripe.exception.CardException;
import com.stripe.exception.StripeException;
import com.stripe.model.Subscription;
import com.stripe.model.SubscriptionSchedule;
import com.stripe.param.SubscriptionCancelParams;
import com.stripe.param.SubscriptionScheduleCreateParams;
import com.stripe.param.SubscriptionScheduleUpdateParams;
import com.stripe.param.SubscriptionUpdateParams;

import lombok.extern.slf4j.Slf4j;

import static com.asrevo.cvhome.billing.commons.StripeRequestOperation.SCHEDULE_CREATE;
import static com.asrevo.cvhome.billing.commons.StripeRequestOperation.SUBSCRIPTION_CANCEL;
import static com.asrevo.cvhome.billing.commons.StripeRequestOperation.SUBSCRIPTION_RESUME;
import static com.asrevo.cvhome.billing.commons.StripeRequestOperation.SUBSCRIPTION_UPDATE;

/**
 * Changes to a subscription that already exists: moving plan, switching renewal off, ending it.
 *
 * <p>
 * Upgrades and downgrades are deliberately not symmetric, because money makes them different. Moving up is charged
 * now, so it happens now. Moving down would take away something already paid for, so it waits until the paid period
 * runs out.
 * </p>
 */
@Slf4j
@Component
public class StripeSubscriptionGateway extends StripeGatewaySupport {

    /**
     * Joins the two halves of an idempotency subject — the subscription being changed and what it is changing to.
     */
    private static final String SUBJECT = "%s:%s";

    /**
     * The schedule states Stripe will accept a release for. Anything else already is, or has passed, the state a
     * release is trying to reach.
     */
    private static final Set<String> RELEASABLE_STATUSES = Set.of("not_started", "active");

    public StripeSubscriptionGateway(StripeCredentials credentials, StripeRequestRepository stripeRequestRepository,
                                     StripeClient stripe) {
        super(credentials, stripeRequestRepository, stripe);
    }

    /**
     * Moves the subscription to a more expensive price, now, charging the difference.
     *
     * <p>
     * {@code ALWAYS_INVOICE} with {@code ERROR_IF_INCOMPLETE} makes the proration invoice settle synchronously, so a
     * declined card comes back here as a {@link CardException} rather than leaving the customer on the new plan with
     * an unpaid invoice. That is what lets the caller refuse the upgrade cleanly instead of half-applying it.
     * </p>
     *
     * @throws SubscriptionChangeRejectedException the card was refused; the subscription is unchanged
     * @throws BillingProviderUnavailableException nothing was decided; the change may or may not have landed, so the
     *                                             caller must not write a local plan change
     */
    public void upgradeNow(StoreMerchantId store, StripeSubscriptionId subscription, PlanPriceEntity target)
            throws SubscriptionChangeRejectedException, BillingProviderUnavailableException {
        String key = idempotencyKey(SUBSCRIPTION_UPDATE,
                String.format(SUBJECT, subscription.id(), target.getStripePriceId().id()));
        recordIntent(key, store, SUBSCRIPTION_UPDATE);
        try {
            Subscription current = stripe().subscriptions().retrieve(subscription.id(), readOptions());
            String itemId = current.getItems().getData().getFirst().getId();
            SubscriptionUpdateParams params = SubscriptionUpdateParams.builder()
                    .addItem(SubscriptionUpdateParams.Item.builder()
                            .setId(itemId)
                            .setPrice(target.getStripePriceId().id())
                            .build())
                    .setProrationBehavior(SubscriptionUpdateParams.ProrationBehavior.ALWAYS_INVOICE)
                    .setPaymentBehavior(SubscriptionUpdateParams.PaymentBehavior.ERROR_IF_INCOMPLETE)
                    .build();
            stripe().subscriptions().update(subscription.id(), params, options(key));
            recordCompletion(key, subscription.id());
            log.info("Upgraded store {} to {} immediately", store, target.getStripePriceId().id());
        } catch (CardException e) {
            throw SubscriptionChangeRejectedException.of(STRIPE, store, target.getId(), e.getCode(), statusOf(e), e);
        } catch (StripeException e) {
            throw BillingProviderUnavailableException.of(STRIPE, store, SUBSCRIPTION_UPDATE, e.getCode(),
                    statusOf(e), e);
        }
    }

    /**
     * Arranges for the subscription to move to a cheaper price when the paid period ends.
     *
     * <p>
     * Built as a Stripe subscription schedule: a first phase holding the current price until
     * {@code current_period_end}, then a second on the target. Stripe drives the switch and reports it as
     * {@code customer.subscription.updated}, which is what applies the change locally — the job that also watches for
     * it is only there for a webhook that never arrives.
     * </p>
     *
     * <p>
     * No {@link CardException} branch, because nothing is charged now. The customer keeps what they paid for and the
     * cheaper price is billed at the next renewal like any other.
     * </p>
     *
     * @return the schedule Stripe created, so it can be released if the downgrade is called off
     * @throws BillingProviderUnavailableException nothing was decided
     */
    public StripeScheduleId scheduleDowngrade(StoreMerchantId store, StripeSubscriptionId subscription,
                                              PlanPriceEntity target, Instant effectiveAt)
            throws BillingProviderUnavailableException {
        String key = idempotencyKey(SCHEDULE_CREATE,
                String.format(SUBJECT, subscription.id(), target.getStripePriceId().id()));
        recordIntent(key, store, SCHEDULE_CREATE);
        try {
            SubscriptionSchedule schedule = scheduleFor(subscription, key);
            SubscriptionSchedule.Phase existing = schedule.getPhases().getFirst();
            SubscriptionSchedule updated = stripe().subscriptionSchedules().update(schedule.getId(),
                    SubscriptionScheduleUpdateParams.builder()
                            .addPhase(SubscriptionScheduleUpdateParams.Phase.builder()
                                    .addItem(SubscriptionScheduleUpdateParams.Phase.Item.builder()
                                            .setPrice(existing.getItems().getFirst().getPrice())
                                            .setQuantity(1L)
                                            .build())
                                    .setStartDate(existing.getStartDate())
                                    .setEndDate(effectiveAt.getEpochSecond())
                                    .build())
                            .addPhase(SubscriptionScheduleUpdateParams.Phase.builder()
                                    .addItem(SubscriptionScheduleUpdateParams.Phase.Item.builder()
                                            .setPrice(target.getStripePriceId().id())
                                            .setQuantity(1L)
                                            .build())
                                    // No end: the cheaper price simply becomes the ongoing one. The schedule is
                                    // released once it reaches this phase, handing the subscription back to normal
                                    // renewal rather than leaving it under a schedule forever.
                                    .build())
                            .setEndBehavior(SubscriptionScheduleUpdateParams.EndBehavior.RELEASE)
                            .build(),
                    readOptions());
            recordCompletion(key, updated.getId());
            log.info("Scheduled store {} to move to {} at {}", store, target.getStripePriceId().id(), effectiveAt);
            return new StripeScheduleId(updated.getId());
        } catch (StripeException e) {
            throw BillingProviderUnavailableException.of(STRIPE, store, SCHEDULE_CREATE, e.getCode(), statusOf(e), e);
        }
    }

    /**
     * The schedule to write phases into: the one the subscription already has, or a new one built from it.
     *
     * <p>
     * Reusing an existing schedule is not an optimisation. Stripe refuses to create a second schedule for a
     * subscription, so without this a downgrade could never be re-requested or corrected once one existed — including
     * after a local write failed with the provider already changed, which is precisely when a retry matters.
     * </p>
     */
    private SubscriptionSchedule scheduleFor(StripeSubscriptionId subscription, String idempotencyKey)
            throws StripeException {
        String existing = stripe().subscriptions().retrieve(subscription.id(), readOptions()).getSchedule();
        if (existing != null) {
            return stripe().subscriptionSchedules().retrieve(existing, readOptions());
        }
        return stripe().subscriptionSchedules().create(
                SubscriptionScheduleCreateParams.builder().setFromSubscription(subscription.id()).build(),
                options(idempotencyKey));
    }

    /**
     * Drops a schedule so the subscription goes back to renewing on its current price.
     *
     * <p>
     * Released rather than cancelled: cancelling a schedule would end the subscription with it, which is emphatically
     * not what calling off a downgrade means.
     * </p>
     */
    public void releaseSchedule(StoreMerchantId store, StripeScheduleId schedule)
            throws BillingProviderUnavailableException {
        String key = idempotencyKey(SUBSCRIPTION_RESUME, schedule.id());
        recordIntent(key, store, SUBSCRIPTION_RESUME);
        try {
            SubscriptionSchedule current = stripe().subscriptionSchedules().retrieve(schedule.id(), readOptions());
            if (!RELEASABLE_STATUSES.contains(current.getStatus())) {
                // Already released, or already ran its course. The end state this asks for is the one that holds, so
                // treating it as a failure would turn a retry — or a local row that lagged behind — into a dead end.
                log.info("Schedule on store {} is {} and needs no release", store, current.getStatus());
                recordCompletion(key, schedule.id());
                return;
            }
            stripe().subscriptionSchedules().release(schedule.id(), options(key));
            recordCompletion(key, schedule.id());
            log.info("Released the pending schedule on store {}", store);
        } catch (StripeException e) {
            throw BillingProviderUnavailableException.of(STRIPE, store, SUBSCRIPTION_RESUME, e.getCode(),
                    statusOf(e), e);
        }
    }

    /**
     * Switches renewal off or back on.
     *
     * <p>
     * Switching off is not cancelling: the subscription stays active and the customer keeps everything until the
     * period they already paid for runs out. Stripe ends it there and sends
     * {@code customer.subscription.deleted}.
     * </p>
     */
    public void setRenewal(StoreMerchantId store, StripeSubscriptionId subscription, boolean renew)
            throws BillingProviderUnavailableException {
        String key = idempotencyKey(SUBSCRIPTION_UPDATE, String.format("%s:renew:%s", subscription.id(), renew));
        recordIntent(key, store, SUBSCRIPTION_UPDATE);
        try {
            stripe().subscriptions().update(subscription.id(),
                    SubscriptionUpdateParams.builder().setCancelAtPeriodEnd(!renew).build(), options(key));
            recordCompletion(key, subscription.id());
            log.info("Store {} renewal switched {}", store, renew ? "on" : "off");
        } catch (StripeException e) {
            throw BillingProviderUnavailableException.of(STRIPE, store, SUBSCRIPTION_UPDATE, e.getCode(),
                    statusOf(e), e);
        }
    }

    /**
     * Ends the subscription now, throwing away time the customer has already paid for.
     *
     * <p>
     * Reserved for administrators. Self-serve cancellation always runs to the end of the paid period, because taking
     * away something already bought is not a thing a customer should be able to do to themselves by accident.
     * </p>
     */
    public void cancelNow(StoreMerchantId store, StripeSubscriptionId subscription)
            throws BillingProviderUnavailableException {
        String key = idempotencyKey(SUBSCRIPTION_CANCEL, subscription.id());
        recordIntent(key, store, SUBSCRIPTION_CANCEL);
        try {
            stripe().subscriptions().cancel(subscription.id(), SubscriptionCancelParams.builder().build(),
                    options(key));
            recordCompletion(key, subscription.id());
            log.info("Store {} subscription cancelled immediately", store);
        } catch (StripeException e) {
            throw BillingProviderUnavailableException.of(STRIPE, store, SUBSCRIPTION_CANCEL, e.getCode(),
                    statusOf(e), e);
        }
    }

}
