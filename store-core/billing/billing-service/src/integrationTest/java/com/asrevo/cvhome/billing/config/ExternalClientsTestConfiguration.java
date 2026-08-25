package com.asrevo.cvhome.billing.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import com.asrevo.cvhome.billing.api.BillingFixtures;
import com.asrevo.cvhome.billing.commons.StripeCustomerId;
import com.asrevo.cvhome.billing.commons.StripeScheduleId;
import com.asrevo.cvhome.billing.repository.PlanPriceRepository;
import com.asrevo.cvhome.billing.repository.PlanRepository;
import com.asrevo.cvhome.billing.repository.StoreSubscriptionRepository;
import com.asrevo.cvhome.billing.service.stripe.StripeCatalogGateway;
import com.asrevo.cvhome.billing.service.stripe.StripeCheckoutGateway;
import com.asrevo.cvhome.billing.service.stripe.StripeCustomerGateway;
import com.asrevo.cvhome.billing.service.stripe.StripeSubscriptionGateway;

import static org.mockito.ArgumentMatchers.any;

/**
 * Stripe, stubbed.
 *
 * <p>
 * The four gateways are the only things in this service that leave the JVM. Everything else an integration test
 * exercises — the permission evaluator, the tenant-scoped queries, the state machine, the outbox — is real, which is
 * the point: what these tests are for is the wiring between them, not Stripe's API.
 * </p>
 *
 * <p>
 * Declared {@code @Primary} rather than {@code @MockitoBean}, following catalog's configuration of the same name:
 * every billing integration test imports this one class, so they share a single Spring context and a single Postgres
 * container instead of forking a context per test class. A {@code @MockitoBean} would give each class its own
 * context key and multiply the containers by the number of test classes.
 * </p>
 *
 * <p>
 * The stubs answer rather than throw. A test that needs a refusal re-stubs the one call it is about, which keeps the
 * failure it is testing visible in the test rather than buried here.
 * </p>
 */
@TestConfiguration(proxyBeanMethods = false)
public class ExternalClientsTestConfiguration {

    /** The customer every org is given, so a checkout has something to bind. */
    public static final String CUSTOMER_ID = "cus_integration_test";

    /** Where a checkout says to send the customer. */
    public static final String CHECKOUT_URL = "https://checkout.stripe.test/cs_integration_test";

    /** The schedule a deferred downgrade is backed by. */
    public static final String SCHEDULE_ID = "sub_sched_integration_test";

    @Bean
    @Primary
    StripeCustomerGateway stubStripeCustomerGateway() throws Exception {
        StripeCustomerGateway gateway = Mockito.mock(StripeCustomerGateway.class);
        Mockito.when(gateway.findOrCreate(any(), any())).thenReturn(new StripeCustomerId(CUSTOMER_ID));
        return gateway;
    }

    @Bean
    @Primary
    StripeCheckoutGateway stubStripeCheckoutGateway() throws Exception {
        StripeCheckoutGateway gateway = Mockito.mock(StripeCheckoutGateway.class);
        Mockito.when(gateway.createSubscriptionSession(any(), any(), any(), any(), any(), any()))
                .thenReturn(CHECKOUT_URL);
        return gateway;
    }

    @Bean
    @Primary
    StripeSubscriptionGateway stubStripeSubscriptionGateway() throws Exception {
        StripeSubscriptionGateway gateway = Mockito.mock(StripeSubscriptionGateway.class);
        Mockito.when(gateway.scheduleDowngrade(any(), any(), any(), any()))
                .thenReturn(new StripeScheduleId(SCHEDULE_ID));
        return gateway;
    }

    /**
     * Never actually called: {@code catalog.stripe-sync-enabled} is false, so {@code PlanCatalogPublisher} is not
     * even a bean. Stubbed anyway so that switching the flag on in a test never reaches Stripe.
     */
    @Bean
    @Primary
    StripeCatalogGateway stubStripeCatalogGateway() {
        return Mockito.mock(StripeCatalogGateway.class);
    }

    /**
     * The arrange step of every integration test, declared here rather than annotated {@code @Component} so that
     * component scanning never reaches into the test source set.
     */
    @Bean
    BillingFixtures billingFixtures(PlanRepository plans, PlanPriceRepository prices,
                                    StoreSubscriptionRepository subscriptions) {
        return new BillingFixtures(plans, prices, subscriptions);
    }

}
