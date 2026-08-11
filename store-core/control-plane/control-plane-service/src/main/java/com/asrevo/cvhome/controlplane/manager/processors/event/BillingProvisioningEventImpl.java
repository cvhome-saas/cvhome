package com.asrevo.cvhome.controlplane.manager.processors.event;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.billing.api.errors.BillingApiUnavailableException;
import com.asrevo.cvhome.billing.api.errors.StoreQuotaRefusedException;
import com.asrevo.cvhome.billing.commons.dto.ProvisionSubscriptionRequest;
import com.asrevo.cvhome.billing.commons.dto.SubscriptionView;
import com.asrevo.cvhome.billing.services.quota.ExternalStoreQuotaService;
import com.asrevo.cvhome.commons.event.EventImpl;
import com.asrevo.cvhome.controlplane.manager.commons.event.store.StoreCreatedEvent;
import com.asrevo.cvhome.errors.UncheckedBaseException;

import io.namastack.outbox.annotation.OutboxHandler;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Gives a newly created store its subscription.
 *
 * <p>
 * A second handler on {@code StoreCreatedEvent}, alongside the one that provisions the store in its pod. The outbox
 * writes one record per handler, so the two retry independently: a pod that is slow to answer cannot stop the store
 * being billed, and billing being down cannot stop the store being built.
 * </p>
 *
 * <p>
 * Deliberately not a call inside {@code createStore}. Doing it there would leave a window in which the store row is
 * committed and its subscription is not — a store nobody is billed for, which nothing would ever notice. Here it
 * inherits the outbox's retries, and billing's {@code provision} is idempotent so a retry is free.
 * </p>
 */
@Service
@AllArgsConstructor
@Slf4j
public class BillingProvisioningEventImpl implements EventImpl<StoreCreatedEvent> {

    private static final String REFUSAL_MESSAGE = """
            Billing refused to provision store {} of org {}: the store exists with no subscription, so an operator \
            has to settle the org before it can be used""";

    private final ExternalStoreQuotaService billingQuotaService;

    /**
     * A refusal is swallowed and an unreachable billing service is not, and that split is the point.
     *
     * <p>
     * {@code BillingApiUnavailableException} propagates so the outbox retries — billing was down, and the store still
     * needs its subscription. A refusal will be refused identically forever, so retrying it would burn the record's
     * attempts and bury the one line an operator has to see.
     * </p>
     */
    @Override
    @OutboxHandler
    public void process(StoreCreatedEvent event) {
        try {
            SubscriptionView subscription = billingQuotaService
                    .provision(new ProvisionSubscriptionRequest(event.orgId(), event.store()));
            log.info("Store {} provisioned in billing as {}", event.store(), subscription.status());
        } catch (StoreQuotaRefusedException e) {
            log.error(REFUSAL_MESSAGE, event.store(), event.orgId(), e);
        } catch (BillingApiUnavailableException e) {
            // Rethrown unchecked because the handler signature cannot declare it. The outbox sees a failure and
            // retries, which is exactly right: billing was unreachable, not unwilling.
            throw new UncheckedBaseException(e);
        }
    }

}
