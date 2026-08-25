package com.asrevo.cvhome.billing.service.stripe;

import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.asrevo.cvhome.billing.commons.StripeCustomerId;
import com.asrevo.cvhome.billing.commons.StripeRequestOperation;
import com.asrevo.cvhome.billing.commons.errors.BillingProviderUnavailableException;
import com.asrevo.cvhome.billing.config.StripeCredentials;
import com.asrevo.cvhome.billing.repository.StoreSubscriptionRepository;
import com.asrevo.cvhome.billing.repository.StripeRequestRepository;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.param.CustomerCreateParams;

import lombok.extern.slf4j.Slf4j;

/**
 * The org's Stripe customer.
 *
 * <p>
 * One customer per <em>org</em>, with a subscription per store underneath it. Billing details, the payment method and
 * the customer portal are all things an org owns once rather than a store owns each; splitting them per store would
 * ask a customer to enter the same card for every store they open.
 * </p>
 *
 * <p>
 * The consequence to be aware of: one failed payment method can put several of an org's stores into
 * {@code PAST_DUE} together, so any notification has to name which stores it means.
 * </p>
 */
@Slf4j
@Component
public class StripeCustomerGateway extends StripeGatewaySupport {

    private static final String ORG_METADATA_KEY = "orgId";

    private final StoreSubscriptionRepository subscriptionRepository;

    public StripeCustomerGateway(StripeCredentials credentials, StripeRequestRepository stripeRequestRepository,
                                 StoreSubscriptionRepository subscriptionRepository) {
        super(credentials, stripeRequestRepository);
        this.subscriptionRepository = subscriptionRepository;
    }

    /**
     * The org's customer, creating one the first time.
     *
     * <p>
     * Looked up from our own rows rather than through Stripe's customer search, which tenancy uses. Search is
     * eventually consistent: two stores created for one org moments apart would both miss and both create a customer,
     * and the org would end up with its payment methods split across two.
     * </p>
     *
     * <p>
     * The idempotency key is derived from the org alone, with no time component, because "this org's customer" is a
     * fact that should never be created twice however far apart the attempts are.
     * </p>
     *
     * @throws BillingProviderUnavailableException Stripe could not be reached, so it is unknown whether a customer
     *                                             now exists
     */
    public StripeCustomerId findOrCreate(ManagerOrgId org, String email)
            throws BillingProviderUnavailableException {
        Optional<StripeCustomerId> existing = subscriptionRepository.findCustomerOf(org);
        if (existing.isPresent()) {
            return existing.get();
        }
        String key = String.format("customer:%s", org.getId());
        recordIntent(key, null, StripeRequestOperation.CUSTOMER_CREATE);
        CustomerCreateParams params = CustomerCreateParams.builder()
                .setEmail(email)
                .putAllMetadata(Map.of(ORG_METADATA_KEY, org.getId().toString()))
                .build();
        try {
            Customer customer = Customer.create(params, options(key));
            recordCompletion(key, customer.getId());
            log.info("Created Stripe customer for org {}", org);
            return new StripeCustomerId(customer.getId());
        } catch (StripeException e) {
            // No CardException branch here on purpose: creating a customer takes no payment, so there is no refusal
            // to distinguish. Every failure of this call is an unknown outcome.
            throw BillingProviderUnavailableException.of(STRIPE, null, StripeRequestOperation.CUSTOMER_CREATE,
                    e.getCode(), statusOf(e), e);
        }
    }

}
