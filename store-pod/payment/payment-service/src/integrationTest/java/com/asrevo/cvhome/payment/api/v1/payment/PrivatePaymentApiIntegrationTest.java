package com.asrevo.cvhome.payment.api.v1.payment;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;

import com.asrevo.cvhome.checkout.api.errors.CheckoutApiUnavailableException;
import com.asrevo.cvhome.checkout.services.order.ExternalOrderSignalService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.payment.config.ExternalClientsTestConfiguration;
import com.asrevo.cvhome.store.core.entity.common.PaymentStatus;
import com.asrevo.cvhome.testsupport.annotations.StorageIntegrationTest;
import com.asrevo.cvhome.testsupport.security.TestJwtSigner;
import com.asrevo.cvhome.testsupport.security.Tokens;

import tools.jackson.databind.JsonNode;

import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.CONTENT;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.GATEWAY_REF;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.INITIATE;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.INTERNAL_REF;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.MANUAL_TRANSFER;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.ORDER;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.PAID;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.PENDING;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.REQUEST_REF;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.STATUS;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.TRANSACTION;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.TRANSACTIONS;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.expect;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.json;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.path;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.paymentRequestBody;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.query;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.scoped;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.slug;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

/**
 * The seller's transaction list and the manual approve/reject decisions behind an offline payment method.
 *
 * <p>
 * Approving a bank transfer is the one place where payment writes back to checkout: the transaction registers a
 * domain event, the outbox picks it up, and the order is told it is paid. That hop runs asynchronously, so it is
 * asserted through the stubbed {@code ExternalOrderSignalService} rather than assumed from the 200.
 * </p>
 */
@StorageIntegrationTest
@Import(ExternalClientsTestConfiguration.class)
class PrivatePaymentApiIntegrationTest {

    /** The store this class stages transactions in. */
    private static final String STORE = Tokens.STORE_1;

    /** A principal on a different store, for the isolation cases. */
    private static final String OTHER_STORE = Tokens.STORE_2;

    private static final String AMOUNT = "17.25";

    private static final String APPROVE = "approve";

    private static final String REJECT = "reject";

    private static final String TOTAL_ELEMENTS = "totalElements";

    private static final String TRANSACTION_NO = "BANK-990";

    private static final Duration PROPAGATION_TIMEOUT = Duration.ofSeconds(30);

    private static final Duration ONE_HOUR = Duration.ofHours(1);

    private static final String BY_REQUEST_REF = "requestRef=%s";

    private static final String APPROVAL_BODY = """
            {"transactionNo":"%s"}""";

    @LocalServerPort
    private int port;

    @Autowired
    private TestJwtSigner signer;

    @Autowired
    private ExternalOrderSignalService externalOrderService;

    private PaymentApiTestSupport api;

    @BeforeEach
    void setUp() {
        api = new PaymentApiTestSupport(port, signer);
    }

    /** Stages a pending manual-transfer transaction and answers its internal reference. */
    private String stage(String ref) {
        var response = api.post(scoped(INITIATE, STORE), api.s2s(), paymentRequestBody(ref, AMOUNT, MANUAL_TRANSFER));
        expect(response, HttpStatus.OK);
        return json(response).get(GATEWAY_REF).asString();
    }

    private JsonNode list(String queryString) {
        var response = api.get(query(scoped(TRANSACTIONS, STORE), queryString), api.admin(STORE));
        expect(response, HttpStatus.OK);
        return json(response);
    }

    private static JsonNode only(JsonNode page) {
        assertThat(page.get(TOTAL_ELEMENTS).asInt()).isEqualTo(1);
        return page.get(CONTENT).get(0);
    }

    @Test
    void aStagedTransactionIsListedWithItsOrderReferenceAndAmount() {
        String ref = slug(ORDER);
        String internalRef = stage(ref);

        JsonNode row = only(list(String.format(BY_REQUEST_REF, ref)));

        assertThat(row.get(INTERNAL_REF).asString()).isEqualTo(internalRef);
        assertThat(row.get(REQUEST_REF).asString()).isEqualTo(ref);
        assertThat(row.get(STATUS).asString()).isEqualTo(PENDING);
        assertThat(row.get("amount").asDouble()).isEqualTo(17.25);
    }

    @Test
    void everyFilterNarrowsTheSamePageAndAnImpossibleCombinationIsEmpty() {
        String ref = slug(ORDER);
        String internalRef = stage(ref);
        Instant now = Instant.now();

        assertThat(only(list(String.format("internalRef=%s&paymentType=%s&status=%s", internalRef, MANUAL_TRANSFER,
                PENDING))).get(REQUEST_REF).asString()).isEqualTo(ref);
        assertThat(only(list(String.format("requestRef=%s&transactionDateFrom=%s&transactionDateTo=%s", ref,
                now.minus(ONE_HOUR), now.plus(ONE_HOUR)))).get(INTERNAL_REF).asString())
                .isEqualTo(internalRef);
        // Same row, asked for after it existed: the date bound is applied, not ignored.
        assertThat(list(String.format("requestRef=%s&transactionDateFrom=%s", ref, now.plus(ONE_HOUR)))
                .get(TOTAL_ELEMENTS).asInt()).isZero();
    }

    @Test
    void approvingATransferMarksItPaidAndTellsCheckout() throws CheckoutApiUnavailableException {
        String ref = slug(ORDER);
        String internalRef = stage(ref);

        expect(api.post(scoped(path(TRANSACTION, internalRef, APPROVE), STORE), api.admin(STORE),
                String.format(APPROVAL_BODY, TRANSACTION_NO)), HttpStatus.OK);

        JsonNode row = only(list(String.format(BY_REQUEST_REF, ref)));
        assertThat(row.get(STATUS).asString()).isEqualTo(PAID);
        assertThat(row.get("transactionNo").asString()).isEqualTo(TRANSACTION_NO);
        verify(externalOrderService, timeout(PROPAGATION_TIMEOUT.toMillis()))
                .signalPayment(eq(new StoreMerchantId(STORE)), eq(ref), argThat(signal -> signal.status() == PaymentStatus.PAID));
    }

    @Test
    void rejectingATransferLeavesItRejectedAndUnpaid() {
        String ref = slug(ORDER);
        String internalRef = stage(ref);

        expect(api.post(scoped(path(TRANSACTION, internalRef, REJECT), STORE), api.admin(STORE), null),
                HttpStatus.OK);

        assertThat(only(list(String.format(BY_REQUEST_REF, ref))).get(STATUS).asString()).isEqualTo("REJECTED");
    }

    @Test
    void anApprovalWithoutATransactionNumberIsRejectedAsInvalid() {
        String internalRef = stage(slug(ORDER));

        expect(api.post(scoped(path(TRANSACTION, internalRef, APPROVE), STORE), api.admin(STORE), "{}"),
                HttpStatus.BAD_REQUEST);
    }

    @Test
    void anotherStoresAdministratorCanNeitherListNorSettleThisStoresPayments() {
        String internalRef = stage(slug(ORDER));
        String intruder = api.admin(OTHER_STORE);

        expect(api.get(scoped(TRANSACTIONS, STORE), intruder), HttpStatus.FORBIDDEN);
        expect(api.post(scoped(path(TRANSACTION, internalRef, APPROVE), STORE), intruder,
                String.format(APPROVAL_BODY, TRANSACTION_NO)), HttpStatus.FORBIDDEN);
        expect(api.post(scoped(path(TRANSACTION, internalRef, REJECT), STORE), intruder, null), HttpStatus.FORBIDDEN);
    }

    @Test
    void aModeratorMaySeeNothingOfTheTransactionLedger() {
        expect(api.get(scoped(TRANSACTIONS, STORE), api.moderator(STORE)), HttpStatus.FORBIDDEN);
    }

}
