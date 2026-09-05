package com.asrevo.cvhome.payment.service.processor;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import com.asrevo.cvhome.commons.domain.CurrencyCode;
import com.asrevo.cvhome.payment.config.ExternalClientsTestConfiguration;
import com.asrevo.cvhome.payment.entity.payment.PaymentSecret;
import com.asrevo.cvhome.payment.errors.PaymentInitiateRejectedException;
import com.asrevo.cvhome.payment.errors.PaymentProviderUnavailableException;
import com.asrevo.cvhome.payment.model.payment.PaymentInitiateResult;
import com.asrevo.cvhome.payment.model.payment.PaymentRequest;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;
import com.asrevo.cvhome.testsupport.annotations.ServiceIntegrationTest;
import com.stripe.exception.ApiConnectionException;
import com.stripe.exception.CardException;
import com.stripe.exception.StripeException;
import com.stripe.model.StripeObject;
import com.stripe.net.ApiMode;
import com.stripe.net.ApiResource;
import com.stripe.net.BaseAddress;
import com.stripe.net.RequestOptions;
import com.stripe.net.StripeResponseGetter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The Stripe payment processor, run for real with the network taken out.
 *
 * <p>
 * This is the same refused-against-no-answer split billing's gateways carry, and here it decides an order's fate: a
 * {@code CardException} is the shopper's card being refused — they must use another one, and retrying unchanged
 * will be refused again — while every other {@code StripeException} settles nothing, and calling one of those a
 * rejection tells the caller to cancel an order that may well have been charged. The processor's own comments say
 * exactly that; this makes them enforceable.
 * </p>
 *
 * <p>
 * Unlike billing, this processor calls Stripe's <em>static</em> resource methods rather than an injected
 * {@code StripeClient}, so the seam here is the SDK's global response getter. It is set and restored per test,
 * which is why this class does not run in parallel with anything else that talks to Stripe.
 * </p>
 */
@ServiceIntegrationTest
// Same context as every other payment integration test: without this the class asks for a second one, which
// has no stubbed external clients and fails to start — and forks another Postgres container besides.
@Import(ExternalClientsTestConfiguration.class)
class StripeProcessorContractIntegrationTest {

    private static final String REFERENCE = "internal-ref-1";

    private static final String ORDER_REF = "order-1";

    private static final String SESSION_JSON = """
            {"id":"cs_test_1","object":"checkout.session","url":"https://checkout.stripe.test/cs_test_1"}""";

    @Autowired
    private StripeProcessor processor;

    private StripeResponseGetter original;

    private StubResponses stripe;

    @BeforeEach
    void setUp() {
        original = ApiResource.getGlobalResponseGetter();
        stripe = new StubResponses();
        ApiResource.setGlobalResponseGetter(stripe);
    }

    @AfterEach
    void tearDown() {
        ApiResource.setGlobalResponseGetter(original);
    }

    @Test
    void asessionIsCreatedAndItsUrlAndIdComeBack() throws Exception {
        stripe.thenJson(SESSION_JSON);

        PaymentInitiateResult result = processor.initiate(REFERENCE, secret(), request());

        assertThat(result.redirectUrl()).isEqualTo("https://checkout.stripe.test/cs_test_1");
        assertThat(result.externalId()).isEqualTo("cs_test_1");
    }

    @Test
    void arefusedCardIsArejectionTheShopperCanActOn() {
        stripe.thenDeclined();

        // Retrying this request unchanged will be refused again; the shopper must use another card.
        assertThatThrownBy(() -> processor.initiate(REFERENCE, secret(), request()))
                .isInstanceOf(PaymentInitiateRejectedException.class)
                .isNotInstanceOf(PaymentProviderUnavailableException.class);
    }

    @Test
    void anunreachableStripeSettlesNothingAndMustNotReadAsArejection() {
        stripe.thenUnreachable();

        // Calling this a rejection would cancel an order that may well have been charged.
        assertThatThrownBy(() -> processor.initiate(REFERENCE, secret(), request()))
                .isInstanceOf(PaymentProviderUnavailableException.class)
                .isNotInstanceOf(PaymentInitiateRejectedException.class);
    }

    @Test
    void theamountIsSentToStripeInItsSmallestUnit() throws Exception {
        stripe.thenJson(SESSION_JSON);

        processor.initiate(REFERENCE, secret(), request());

        // 12.34 is 1234 cents. This read `amount.longValue() * 100`, which truncates: the shopper was charged
        // 12.00 and nothing downstream could tell, because the number Stripe got was internally consistent.
        assertThat(stripe.lastParams().toString()).contains("1234").doesNotContain("1200");
    }

    @Test
    void theinternalReferenceTravelsSoTheWebhookCanBeMatchedBackToTheOrder() throws Exception {
        stripe.thenJson(SESSION_JSON);

        processor.initiate(REFERENCE, secret(), request());

        // Without it the webhook that confirms payment cannot be tied to anything.
        assertThat(stripe.lastParams().toString()).contains(REFERENCE);
    }

    @Test
    void theprocessorNamesItselfAsTheStripeOne() {
        assertThat(processor.type()).isEqualTo(PaymentType.STRIPE);
    }

    private static PaymentRequest request() {
        return PaymentRequest.builder()
                .ref(ORDER_REF)
                .amount(new BigDecimal("12.34"))
                .currency(new CurrencyCode("USD"))
                .paymentType(PaymentType.STRIPE)
                .expireAt(Instant.now().plus(1, ChronoUnit.HOURS))
                .successUrl("https://shop.example/ok")
                .cancelUrl("https://shop.example/no")
                .build();
    }

    private static PaymentSecret secret() {
        return new PaymentSecret() {
            @Override
            public String getApiKey() {
                return "pk_test";
            }

            @Override
            public String getSecretKey() {
                return "sk_test";
            }

            @Override
            public String getWebhookSecret() {
                return "whsec_test";
            }
        };
    }

    /** Stripe's SDK with the network removed, at the global seam its static resource methods read. */
    private static final class StubResponses implements StripeResponseGetter {

        private final Deque<Object> answers = new ArrayDeque<>();

        private Map<String, Object> lastParams;

        StubResponses thenJson(String json) {
            answers.add(json);
            return this;
        }

        StubResponses thenDeclined() {
            answers.add(new CardException("Your card was declined.", null, "card_declined", null, null, null, 402,
                    null));
            return this;
        }

        StubResponses thenUnreachable() {
            answers.add(new ApiConnectionException("Could not reach Stripe."));
            return this;
        }

        Map<String, Object> lastParams() {
            return lastParams;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T extends StripeObject> T request(BaseAddress baseAddress, ApiResource.RequestMethod method,
                                                  String path, Map<String, Object> params, Type typeToken,
                                                  RequestOptions options, ApiMode apiMode) throws StripeException {
            lastParams = params;
            Object next = answers.poll();
            if (next instanceof StripeException failure) {
                throw failure;
            }
            String json = next instanceof String canned ? canned : "{}";
            return (T) ApiResource.GSON.fromJson(json, typeToken);
        }

        @Override
        public InputStream requestStream(BaseAddress baseAddress, ApiResource.RequestMethod method, String path,
                                         Map<String, Object> params, RequestOptions options, ApiMode apiMode)
                throws StripeException {
            throw new ApiConnectionException("No processor streams from Stripe.");
        }

    }

}
