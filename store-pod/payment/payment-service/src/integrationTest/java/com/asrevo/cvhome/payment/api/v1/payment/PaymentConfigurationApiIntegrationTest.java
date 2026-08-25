package com.asrevo.cvhome.payment.api.v1.payment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import com.asrevo.cvhome.payment.config.ExternalClientsTestConfiguration;
import com.asrevo.cvhome.testsupport.annotations.StorageIntegrationTest;
import com.asrevo.cvhome.testsupport.security.TestJwtSigner;
import com.asrevo.cvhome.testsupport.security.Tokens;

import tools.jackson.databind.JsonNode;

import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.API_KEY;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.CODE;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.ENABLED;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.MANUAL_TRANSFER;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.PAYMENT_TYPE;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.PAYPAL;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.PRIVATE_CONFIG;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.PUBLIC_CONFIG;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.STRIPE;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.SUPPORTED_TYPES;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.WEBHOOK_SECRET;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.configBody;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.expect;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.json;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.path;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.scoped;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * The seller-facing payment configuration surface over real HTTP: the secrets round-trip through the crypto
 * provider, only a store's own administrator may touch them, and a management call for a row that is not there is a
 * typed 404 rather than a silent no-op.
 *
 * <p>
 * Each test class in this domain owns one seeded store for the rows it mutates — this one uses {@code STORE_2} — so
 * the classes can share a context without stepping on each other's fixtures.
 * </p>
 */
@StorageIntegrationTest
@Import(ExternalClientsTestConfiguration.class)
class PaymentConfigurationApiIntegrationTest {

    /** Read-only assertions and the permission cases. */
    private static final String STORE_A = Tokens.STORE_1;

    /** The store whose rows this class creates, updates and deletes. */
    private static final String STORE_B = Tokens.STORE_2;

    private static final String SECRET_KEY_VALUE = "sk_live_rotated";

    private static final String SUPPORTED_STATUSES = "supported-payment-statuses";

    private static final String MANUAL_TRANSFER_API_KEY = "mt-api-key";

    private static final String ROTATED_PAYPAL_API_KEY = "paypal-rotated";

    /** What an intruder would write if the permission gate let them through. */
    private static final String STOLEN = "stolen";

    @LocalServerPort
    private int port;

    @Autowired
    private TestJwtSigner signer;

    private PaymentApiTestSupport api;

    @BeforeEach
    void setUp() {
        api = new PaymentApiTestSupport(port, signer);
    }

    private JsonNode configs(String store) {
        var response = api.get(scoped(PRIVATE_CONFIG, store), api.admin(store));
        expect(response, HttpStatus.OK);
        return json(response);
    }

    private static JsonNode ofType(JsonNode configs, String type) {
        for (JsonNode node : configs) {
            if (type.equals(node.get(PAYMENT_TYPE).asString())) {
                return node;
            }
        }
        throw new AssertionError(String.format("no %s configuration in %s", type, configs));
    }

    @Test
    void everySeededConfigurationComesBackWithItsSecretsDecrypted() {
        JsonNode stripe = ofType(configs(STORE_A), STRIPE);

        assertThat(stripe.get(ENABLED).asBoolean()).isTrue();
        // The seed row stores this key as ENC:1:… — reading it back as plaintext is the whole point of the mapper.
        assertThat(stripe.get("secretKey").asString()).startsWith("sk_");
    }

    @Test
    void savingAConfigurationEncryptsItAndReadsBackTheOriginal() {
        String secret = String.format("whsec_%s", System.nanoTime());

        expect(api.post(scoped(PRIVATE_CONFIG, STORE_B), api.admin(STORE_B),
                configBody(MANUAL_TRANSFER, MANUAL_TRANSFER_API_KEY, SECRET_KEY_VALUE, secret, true)), HttpStatus.OK);

        JsonNode saved = ofType(configs(STORE_B), MANUAL_TRANSFER);
        assertThat(saved.get(WEBHOOK_SECRET).asString()).isEqualTo(secret);
        assertThat(saved.get(API_KEY).asString()).isEqualTo(MANUAL_TRANSFER_API_KEY);
    }

    @Test
    void updatingAConfigurationReplacesTheStoredSecrets() {
        expect(api.send(HttpMethod.PUT, scoped(path(PRIVATE_CONFIG, PAYPAL), STORE_B), api.admin(STORE_B),
                configBody(PAYPAL, ROTATED_PAYPAL_API_KEY, SECRET_KEY_VALUE, "paypal-hook", false)), HttpStatus.OK);

        JsonNode updated = ofType(configs(STORE_B), PAYPAL);
        assertThat(updated.get(API_KEY).asString()).isEqualTo(ROTATED_PAYPAL_API_KEY);
        assertThat(updated.get(ENABLED).asBoolean()).isFalse();
    }

    @Test
    void deletingAConfigurationTwiceIsATypedNotFoundTheSecondTime() {
        String url = scoped(path(PRIVATE_CONFIG, MANUAL_TRANSFER), STORE_B);
        String token = api.admin(STORE_B);
        expect(api.post(scoped(PRIVATE_CONFIG, STORE_B), token,
                configBody(MANUAL_TRANSFER, "temp", SECRET_KEY_VALUE, "temp-hook", true)), HttpStatus.OK);

        expect(api.send(HttpMethod.DELETE, url, token, null), HttpStatus.OK);

        var gone = api.send(HttpMethod.DELETE, url, token, null);
        expect(gone, HttpStatus.NOT_FOUND);
        assertThat(json(gone).get(CODE).asString()).isEqualTo("PAYMENT.CONFIGURATION.NOT_FOUND");
        expect(api.send(HttpMethod.PUT, url, token, configBody(MANUAL_TRANSFER, "x", "y", "z", true)),
                HttpStatus.NOT_FOUND);
    }

    @Test
    void anotherStoresAdministratorSeesNothingOfThisStoresSecrets() {
        expect(api.get(scoped(PRIVATE_CONFIG, STORE_A), api.admin(STORE_B)), HttpStatus.FORBIDDEN);
        expect(api.post(scoped(PRIVATE_CONFIG, STORE_A), api.admin(STORE_B),
                configBody(STRIPE, STOLEN, STOLEN, STOLEN, true)), HttpStatus.FORBIDDEN);
    }

    @Test
    void aModeratorMayNotManagePaymentSecrets() {
        expect(api.get(scoped(PRIVATE_CONFIG, STORE_A), api.moderator(STORE_A)), HttpStatus.FORBIDDEN);
        expect(api.send(HttpMethod.DELETE, scoped(path(PRIVATE_CONFIG, STRIPE), STORE_A), api.moderator(STORE_A), null),
                HttpStatus.FORBIDDEN);
    }

    @Test
    void anUnauthenticatedCallerNeverReachesTheConfigurationApi() {
        expect(api.get(scoped(PRIVATE_CONFIG, STORE_A), null), HttpStatus.UNAUTHORIZED);
    }

    @Test
    void theSupportedTypeAndStatusListsAreTheEnumsThemselves() {
        var types = api.get(scoped(path(PRIVATE_CONFIG, SUPPORTED_TYPES), STORE_A), api.admin(STORE_A));
        expect(types, HttpStatus.OK);
        assertThat(json(types).toString()).contains(STRIPE, PAYPAL, MANUAL_TRANSFER);

        var statuses = api.get(scoped(path(PRIVATE_CONFIG, SUPPORTED_STATUSES), STORE_A), api.admin(STORE_A));
        expect(statuses, HttpStatus.OK);
        assertThat(json(statuses).toString()).contains("PAID", "REFUNDED");
    }

    @Test
    void theStorefrontIsToldOnlyWhichTypesAreEnabled() {
        var response = api.get(path(PUBLIC_CONFIG, STORE_A, SUPPORTED_TYPES), null);

        expect(response, HttpStatus.OK);
        assertThat(json(response).toString()).contains(STRIPE);
    }

}
