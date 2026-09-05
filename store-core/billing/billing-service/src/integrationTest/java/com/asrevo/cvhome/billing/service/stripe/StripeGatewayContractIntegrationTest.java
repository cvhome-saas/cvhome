package com.asrevo.cvhome.billing.service.stripe;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import com.asrevo.cvhome.billing.api.BillingFixtures;
import com.asrevo.cvhome.billing.commons.StripeCustomerId;
import com.asrevo.cvhome.billing.commons.StripeProductId;
import com.asrevo.cvhome.billing.commons.StripeSubscriptionId;
import com.asrevo.cvhome.billing.commons.errors.BillingProviderUnavailableException;
import com.asrevo.cvhome.billing.commons.errors.SubscriptionChangeRejectedException;
import com.asrevo.cvhome.billing.config.ExternalClientsTestConfiguration;
import com.asrevo.cvhome.billing.config.StripeCredentials;
import com.asrevo.cvhome.billing.config.StubStripeResponseGetter;
import com.asrevo.cvhome.billing.domain.PlanPriceEntity;
import com.asrevo.cvhome.billing.repository.StoreSubscriptionRepository;
import com.asrevo.cvhome.billing.repository.StripeRequestRepository;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.testsupport.annotations.ServiceIntegrationTest;
import com.stripe.StripeClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The four Stripe gateways, running for real against an SDK whose network has been taken out.
 *
 * <p>
 * Every other billing integration test mocks these gateways, because what those tests are about is the wiring
 * around them. That leaves the gateways themselves — the only code in the service that leaves the JVM — exercised
 * by nothing. Here they are the real objects, constructed with the real credential resolver and the real
 * {@code StripeRequestRepository}, and only Stripe's {@code StripeResponseGetter} is replaced.
 * </p>
 *
 * <p>
 * <strong>The split under test is refusal against no answer.</strong> A {@code CardException} is Stripe having
 * looked and said no: the change did not happen, the caller may fail it and tell the merchant why. Any other
 * {@code StripeException} means no answer arrived, so whether the change happened is unknown and the caller must
 * leave it recoverable. Collapsing those two is the failure {@code AGENTS.md} names explicitly — it cancels
 * subscriptions that were in fact charged.
 * </p>
 *
 * <p>
 * The idempotency key is asserted alongside, because it is the only thing that makes a retry of an unknown
 * outcome safe: the intent row is written <em>before</em> the call, so a second attempt reuses the same key and
 * Stripe recognises it rather than charging twice.
 * </p>
 */
@ServiceIntegrationTest
@Import(ExternalClientsTestConfiguration.class)
class StripeGatewayContractIntegrationTest {

    private static final String CUS_1 = "cus_1";
    private static final String HTTPS_EXAMPLE_TEST_OK = "https://example.test/ok";
    private static final String HTTPS_EXAMPLE_TEST_NO = "https://example.test/no";
    private static final String PROD_1 = "prod_1";
    private static final String STORE_ID = "65f023632bc46470c104b76f";

    private static final StoreMerchantId STORE = new StoreMerchantId(STORE_ID);

    private static final StripeSubscriptionId SUBSCRIPTION =
            new StripeSubscriptionId(BillingFixtures.PROVIDER_SUBSCRIPTION);

    private static final String SUBSCRIPTION_JSON = """
            {"id":"sub_integration_test","object":"subscription","status":"active",
             "items":{"object":"list","data":[{"id":"si_1","object":"subscription_item"}]}}""";

    private static final String SCHEDULE_JSON = """
            {"id":"sub_sched_1","object":"subscription_schedule","status":"not_started",
             "phases":[{"start_date":1700000000,"end_date":1800000000,
                        "items":[{"price":"price_existing","quantity":1}]}]}""";

    private static final String CARD_DECLINED = "card_declined";

    /** ManagerOrgId wraps a Mongo ObjectId, so these have to be real 24-character hex. */
    private static final String ORG = "65f023632bc46470c104b750";

    private static final String UNMAPPED_ORG = "65f023632bc46470c104b751";

    @Autowired
    private StripeCredentials credentials;

    @Autowired
    private StripeRequestRepository requests;

    @Autowired
    private StoreSubscriptionRepository subscriptions;

    @Autowired
    private BillingFixtures fixtures;

    private StubStripeResponseGetter stripe;

    private StripeSubscriptionGateway subscriptionGateway;

    private StripeCustomerGateway customerGateway;

    private StripeCheckoutGateway checkoutGateway;

    private StripeCatalogGateway catalogGateway;

    private PlanPriceEntity price;

    @BeforeEach
    void setUp() {
        stripe = new StubStripeResponseGetter();
        StripeClient client = new StripeClient(stripe);
        subscriptionGateway = new StripeSubscriptionGateway(credentials, requests, client);
        customerGateway = new StripeCustomerGateway(credentials, requests, client, subscriptions);
        checkoutGateway = new StripeCheckoutGateway(credentials, requests, client);
        catalogGateway = new StripeCatalogGateway(credentials, requests, client);
        // The gateways read the Stripe ids off the catalog rows, and the seed leaves them unpublished.
        fixtures.publishPrices();
        price = fixtures.dearestPrice();
    }

    @Test
    void anupgradeRetrievesTheSubscriptionAndUpdatesItsItem() throws Exception {
        stripe.thenJson(SUBSCRIPTION_JSON).thenJson(SUBSCRIPTION_JSON);

        subscriptionGateway.upgradeNow(STORE, SUBSCRIPTION, price);

        assertThat(stripe.lastPath()).contains(BillingFixtures.PROVIDER_SUBSCRIPTION);
        // The key is written as an intent before the call, so a retry of an unknown outcome reuses it.
        assertThat(stripe.lastIdempotencyKey()).isNotBlank();
        assertThat(requests.existsById(stripe.lastIdempotencyKey())).isTrue();
    }

    @Test
    void adeclinedUpgradeIsArefusalTheCallerMayActOn() {
        stripe.thenJson(SUBSCRIPTION_JSON).thenDeclined(CARD_DECLINED);

        // Stripe looked and said no: nothing changed, and the merchant can be told why.
        assertThatThrownBy(() -> subscriptionGateway.upgradeNow(STORE, SUBSCRIPTION, price))
                .isInstanceOf(SubscriptionChangeRejectedException.class)
                .isNotInstanceOf(BillingProviderUnavailableException.class);
    }

    @Test
    void anunreachableStripeOnAnupgradeIsAnUnknownOutcome() {
        stripe.thenJson(SUBSCRIPTION_JSON).thenUnreachable();

        // No answer arrived, so whether the upgrade happened is unknown and the change must stay recoverable.
        assertThatThrownBy(() -> subscriptionGateway.upgradeNow(STORE, SUBSCRIPTION, price))
                .isInstanceOf(BillingProviderUnavailableException.class)
                .isNotInstanceOf(SubscriptionChangeRejectedException.class);
    }

    @Test
    void adowngradeIsScheduledThroughAsubscriptionSchedule() throws Exception {
        stripe.thenJson(SUBSCRIPTION_JSON).thenJson(SCHEDULE_JSON).thenJson(SCHEDULE_JSON);

        var scheduled = subscriptionGateway.scheduleDowngrade(STORE, SUBSCRIPTION, price,
                Instant.now().plus(30, ChronoUnit.DAYS));

        assertThat(scheduled.id()).isNotBlank();
    }

    @Test
    void anunreachableStripeOnAdowngradeIsAnUnknownOutcome() {
        stripe.thenUnreachable();

        assertThatThrownBy(() -> subscriptionGateway.scheduleDowngrade(STORE, SUBSCRIPTION, price,
                Instant.now().plus(30, ChronoUnit.DAYS)))
                .isInstanceOf(BillingProviderUnavailableException.class);
    }

    @Test
    void renewalIsTurnedOffAndBackOn() throws Exception {
        stripe.thenJson(SUBSCRIPTION_JSON).thenJson(SUBSCRIPTION_JSON);

        assertThatCode(() -> subscriptionGateway.setRenewal(STORE, SUBSCRIPTION, false)).doesNotThrowAnyException();
        assertThatCode(() -> subscriptionGateway.setRenewal(STORE, SUBSCRIPTION, true)).doesNotThrowAnyException();
    }

    @Test
    void anunreachableStripeOnArenewalChangeIsAnUnknownOutcome() {
        stripe.thenUnreachable();

        assertThatThrownBy(() -> subscriptionGateway.setRenewal(STORE, SUBSCRIPTION, false))
                .isInstanceOf(BillingProviderUnavailableException.class);
    }

    @Test
    void asubscriptionIsCancelledNow() throws Exception {
        stripe.thenJson(SUBSCRIPTION_JSON);

        assertThatCode(() -> subscriptionGateway.cancelNow(STORE, SUBSCRIPTION)).doesNotThrowAnyException();
    }

    @Test
    void anunreachableStripeOnAcancelIsAnUnknownOutcome() {
        stripe.thenUnreachable();

        assertThatThrownBy(() -> subscriptionGateway.cancelNow(STORE, SUBSCRIPTION))
                .isInstanceOf(BillingProviderUnavailableException.class);
    }

    /**
     * Creating a customer takes no payment, so there is no refusal to distinguish — every failure of this call is
     * an unknown outcome, and the gateway deliberately has no {@code CardException} branch.
     */
    @Test
    void everyFailureOfAcustomerCreateIsAnUnknownOutcome() {
        stripe.thenUnreachable();

        assertThatThrownBy(() -> customerGateway.findOrCreate(new ManagerOrgId(UNMAPPED_ORG), "a@example.com"))
                .isInstanceOf(BillingProviderUnavailableException.class);
    }

    @Test
    void adeclinedCheckoutSessionIsArefusalRatherThanAnOutage() {
        stripe.thenDeclined(CARD_DECLINED);

        assertThatThrownBy(() -> checkoutGateway.createSubscriptionSession(STORE, new ManagerOrgId(ORG),
                new StripeCustomerId(CUS_1), price,
                HTTPS_EXAMPLE_TEST_OK, HTTPS_EXAMPLE_TEST_NO))
                .isInstanceOf(SubscriptionChangeRejectedException.class);
    }

    @Test
    void anunreachableStripeOnAcheckoutSessionIsAnUnknownOutcome() {
        stripe.thenUnreachable();

        assertThatThrownBy(() -> checkoutGateway.createSubscriptionSession(STORE, new ManagerOrgId(ORG),
                new StripeCustomerId(CUS_1), price,
                HTTPS_EXAMPLE_TEST_OK, HTTPS_EXAMPLE_TEST_NO))
                .isInstanceOf(BillingProviderUnavailableException.class);
    }

    @Test
    void thecatalogPublishesAproductAndReportsAnOutageAsSuch() throws Exception {
        stripe.thenJson("""
                {"id":"prod_1","object":"product"}""");

        assertThat(catalogGateway.createProduct(fixtures.plans().getFirst()).id()).isEqualTo(PROD_1);

        stripe.thenUnreachable();
        assertThatThrownBy(() -> catalogGateway.createProduct(fixtures.plans().getFirst()))
                .isInstanceOf(BillingProviderUnavailableException.class);
    }

    @Test
    void thecatalogPublishesApriceAndReportsAnOutageAsSuch() throws Exception {
        var plan = fixtures.plans().getFirst().publishedAs(new StripeProductId(PROD_1));
        stripe.thenJson("""
                {"id":"price_1","object":"price"}""");

        assertThat(catalogGateway.createPrice(plan, price).id()).isEqualTo("price_1");

        stripe.thenUnreachable();
        assertThatThrownBy(() -> catalogGateway.createPrice(plan, price))
                .isInstanceOf(BillingProviderUnavailableException.class);
    }

}
