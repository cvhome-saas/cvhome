package com.asrevo.cvhome.billing.service.stripe;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.asrevo.cvhome.billing.commons.BillingInterval;
import com.asrevo.cvhome.billing.commons.PlanId;
import com.asrevo.cvhome.billing.commons.StripeCustomerId;
import com.asrevo.cvhome.billing.commons.StripePriceId;
import com.asrevo.cvhome.billing.commons.errors.BillingProviderUnavailableException;
import com.asrevo.cvhome.billing.commons.errors.SubscriptionChangeRejectedException;
import com.asrevo.cvhome.billing.config.StripeCredentials;
import com.asrevo.cvhome.billing.domain.PlanPriceEntity;
import com.asrevo.cvhome.billing.repository.StripeRequestRepository;
import com.asrevo.cvhome.commons.domain.CurrencyCode;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.stripe.StripeClient;
import com.stripe.exception.ApiConnectionException;
import com.stripe.exception.CardException;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.param.checkout.SessionCreateParams;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Opening Stripe's hosted checkout.
 *
 * <p>
 * The store id travels twice, and both copies are load-bearing: {@code client_reference_id} is how
 * {@code checkout.session.completed} is attributed, and the subscription's metadata is how every later
 * {@code customer.subscription.*} event is — those carry no reference at all to the session that created them. Lose
 * either and a whole class of webhook becomes unattributable.
 * </p>
 */
class StripeCheckoutGatewayTest {

    private static final String CS_1 = "cs_1";

    private static final String HTTPS_CHECKOUT_STRIPE_TEST_CS_1 = "https://checkout.stripe.test/cs_1";

    private static final String HTTPS_CONSOLE_NO = "https://console/no";

    private static final String HTTPS_CONSOLE_OK = "https://console/ok";

    private static final String PRICE_PRO_MONTHLY = "price_pro_monthly";

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");

    private static final ManagerOrgId ORG = new ManagerOrgId("32a034a43cd77581d105c87a");

    private static final String CUSTOMER_ID = "cus_1";

    private static final StripeCustomerId CUSTOMER = new StripeCustomerId(CUSTOMER_ID);

    private StripeClient stripe;

    private StripeCheckoutGateway gateway;

    @BeforeEach
    void setUp() {
        StripeCredentials credentials = mock(StripeCredentials.class);
        when(credentials.apiKey()).thenReturn("sk_test_checkout");
        stripe = mock(StripeClient.class, RETURNS_DEEP_STUBS);
        StripeRequestRepository requests = mock(StripeRequestRepository.class);
        when(requests.existsById(anyString())).thenReturn(false);
        when(requests.findById(anyString())).thenReturn(Optional.empty());
        gateway = new StripeCheckoutGateway(credentials, requests, stripe);
    }

    private static PlanPriceEntity price() {
        PlanPriceEntity price = PlanPriceEntity.create(PlanId.newId(), new CurrencyCode("USD"), 3000L,
                BillingInterval.MONTH, 0);
        return price.publishedAs(new StripePriceId(PRICE_PRO_MONTHLY));
    }

    private String open() throws Exception {
        return gateway.createSubscriptionSession(STORE, ORG, CUSTOMER, price(), HTTPS_CONSOLE_OK,
                HTTPS_CONSOLE_NO);
    }

    @Test
    @DisplayName("the session is subscription mode, on the org's customer, and returns its URL")
    void opensASubscriptionSession() throws Exception {
        Session session = mock(Session.class);
        when(session.getId()).thenReturn(CS_1);
        when(session.getUrl()).thenReturn(HTTPS_CHECKOUT_STRIPE_TEST_CS_1);
        when(stripe.checkout().sessions().create(any(SessionCreateParams.class), any(RequestOptions.class)))
                .thenReturn(session);

        String url = open();

        ArgumentCaptor<SessionCreateParams> params = ArgumentCaptor.forClass(SessionCreateParams.class);
        verify(stripe.checkout().sessions()).create(params.capture(), any(RequestOptions.class));
        assertThat(url).isEqualTo(HTTPS_CHECKOUT_STRIPE_TEST_CS_1);
        assertThat(params.getValue().getMode()).isEqualTo(SessionCreateParams.Mode.SUBSCRIPTION);
        assertThat(params.getValue().getCustomer()).isEqualTo(CUSTOMER_ID);
        assertThat(params.getValue().getSuccessUrl()).isEqualTo(HTTPS_CONSOLE_OK);
        assertThat(params.getValue().getCancelUrl()).isEqualTo(HTTPS_CONSOLE_NO);
        assertThat(params.getValue().getLineItems()).singleElement()
                .satisfies(item -> {
                    assertThat(item.getPrice()).isEqualTo(PRICE_PRO_MONTHLY);
                    assertThat(item.getQuantity()).isEqualTo(1L);
                });
    }

    @Test
    @DisplayName("the store id travels both as the client reference and in the subscription's metadata")
    void carriesTheStoreTwice() throws Exception {
        Session session = mock(Session.class);
        when(session.getId()).thenReturn(CS_1);
        when(session.getUrl()).thenReturn(HTTPS_CHECKOUT_STRIPE_TEST_CS_1);
        when(stripe.checkout().sessions().create(any(SessionCreateParams.class), any(RequestOptions.class)))
                .thenReturn(session);

        open();

        ArgumentCaptor<SessionCreateParams> params = ArgumentCaptor.forClass(SessionCreateParams.class);
        verify(stripe.checkout().sessions()).create(params.capture(), any(RequestOptions.class));
        // How checkout.session.completed is attributed.
        assertThat(params.getValue().getClientReferenceId()).isEqualTo(STORE.getId().toString());
        // How every later customer.subscription.* event is — they carry nothing pointing back at this session.
        assertThat(params.getValue().getSubscriptionData().getMetadata())
                .containsEntry("storeId", STORE.getId().toString())
                .containsEntry("orgId", ORG.getId().toString());
    }

    @Test
    @DisplayName("a declined card is a refusal the customer is told about, not a fault")
    void aDeclinedCardIsARejection() throws Exception {
        when(stripe.checkout().sessions().create(any(SessionCreateParams.class), any(RequestOptions.class)))
                .thenThrow(new CardException("declined", "req_1", "card_declined", null, "generic_decline", null,
                        402, null));

        // The one Stripe failure that is an answer rather than a fault. Retrying this request unchanged will be
        // refused again, so the customer is told, not the operator.
        assertThatThrownBy(this::open).isInstanceOf(SubscriptionChangeRejectedException.class);
    }

    @Test
    @DisplayName("anything else settles nothing and must not be reported as a decline")
    void otherFailuresAreUnavailable() throws Exception {
        when(stripe.checkout().sessions().create(any(SessionCreateParams.class), any(RequestOptions.class)))
                .thenThrow(new ApiConnectionException("no route to stripe"));

        // Calling a connection failure a refusal would tell the customer their card failed when it never ran.
        assertThatThrownBy(this::open).isInstanceOf(BillingProviderUnavailableException.class);
    }

    @Test
    @DisplayName("the idempotency key names the store and the price, and buckets by the minute")
    void idempotencyKeyIsScopedAndTimeBucketed() throws Exception {
        Session session = mock(Session.class);
        when(session.getId()).thenReturn(CS_1);
        when(session.getUrl()).thenReturn(HTTPS_CHECKOUT_STRIPE_TEST_CS_1);
        when(stripe.checkout().sessions().create(any(SessionCreateParams.class), any(RequestOptions.class)))
                .thenReturn(session);
        PlanPriceEntity price = price();

        gateway.createSubscriptionSession(STORE, ORG, CUSTOMER, price, HTTPS_CONSOLE_OK, HTTPS_CONSOLE_NO);

        ArgumentCaptor<RequestOptions> options = ArgumentCaptor.forClass(RequestOptions.class);
        verify(stripe.checkout().sessions()).create(any(SessionCreateParams.class), options.capture());
        // Unlike the catalog's keys this one *does* carry time: a double-clicked button inside a minute reuses
        // Stripe's answer, but a deliberate second attempt later gets a fresh session rather than a stale URL.
        assertThat(options.getValue().getIdempotencyKey())
                .startsWith(String.format("checkout_session_create:%s:%s:", STORE.getId(), price.getId().getId()))
                .matches(".*:\\d+$");
    }

}
