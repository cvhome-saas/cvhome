package com.asrevo.cvhome.billing.api;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.asrevo.cvhome.testsupport.http.ApiClient;
import com.asrevo.cvhome.testsupport.security.TestJwtSigner;
import com.asrevo.cvhome.testsupport.security.Tokens;

import tools.jackson.databind.JsonNode;

/**
 * Billing's URL and principal conventions over the shared {@link ApiClient}.
 *
 * <p>
 * Billing is a store-core service, so its endpoints are gated on {@code STORE-CORE.BILLING.*} and split three ways:
 * {@code READ} admits anyone who can read the store, {@code MANAGE} admits only an org admin and the platform
 * operator — spending the org's money is an org-level act, and a store admin is not the person who owns the card —
 * and the platform endpoints admit the super admin alone.
 * </p>
 */
public final class BillingApiSupport {

    /** The tenant under test. Its stores are seeded by {@code init-sql/data-test-stores.sql}. */
    public static final String ORG_A = "32a034a43cd77581d105c87a";

    /** The neighbour. Nothing an org-A principal does may reach it. */
    public static final String ORG_B = "42a034a43cd77581d105c87b";

    public static final String SUBSCRIPTION_STORE = "b1110000000000000000aa01";

    public static final String LIFECYCLE_STORE = "b1110000000000000000aa02";

    public static final String INVOICE_STORE = "b1110000000000000000aa03";

    public static final String WEBHOOK_STORE = "b1110000000000000000aa04";

    public static final String PLATFORM_STORE = "b1110000000000000000aa05";

    public static final String ENTITLEMENT_STORE = "b1110000000000000000aa06";

    /** Org B's store: the target of every cross-tenant case. */
    public static final String NEIGHBOUR_STORE = "b2220000000000000000bb01";

    public static final String V1 = "/api/v1";

    public static final String V2 = "/api/v2";

    private static final long ONE_HOUR = 3600L;

    private final ApiClient client;

    private final Tokens tokens;

    private final TestJwtSigner signer;

    public BillingApiSupport(int port, TestJwtSigner signer) {
        this.client = new ApiClient(port);
        this.tokens = new Tokens(signer);
        this.signer = signer;
    }

    // ------------------------------------------------------------------------------------------------ principals

    /**
     * An org administrator of {@code org}, minted here rather than through {@link Tokens#orgAdmin(String)}.
     *
     * <p>
     * <strong>Not a duplication to be tidied away.</strong> {@code Tokens.orgAdmin} mints the {@code store_core}
     * scope, and {@code SecurityUtils.getOrgStoreIdentity} checks the scope <em>before</em> the role — so that
     * principal resolves to "platform-wide, no org", {@code SubscriptionApi.tenantScopeOf} hands the service a null
     * scope, and every cross-org query stops being narrowed. An isolation test written with it passes whether or not
     * the boundary exists, which is worse than having no test at all.
     * </p>
     *
     * <p>
     * The shape below is what a real org admin's token carries: {@code ROLE_ORG_ADMIN}, the {@code store_pod} scope,
     * and the {@code org} claim the query is narrowed by.
     * </p>
     */
    public String orgAdmin(String org) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "org-admin@" + org);
        claims.put("name", "Test Org Admin");
        claims.put("roles", List.of(Tokens.ROLE_ORG_ADMIN));
        claims.put("scope", Tokens.SCOPE_STORE_POD);
        claims.put("org", org);
        claims.put("exp", Instant.now().plusSeconds(ONE_HOUR).getEpochSecond());
        return signer.sign(claims);
    }

    /** A store administrator: may read the store's billing, may not spend the org's money. */
    public String storeAdmin(String store, String org) {
        return tokens.staff(Tokens.ROLE_STORE_ADMIN, store, org);
    }

    /** A store moderator: the narrowest principal that may still see the plan it works under. */
    public String storeModerator(String store, String org) {
        return tokens.staff(Tokens.ROLE_STORE_MODERATOR, store, org);
    }

    public String superAdmin() {
        return tokens.superAdmin();
    }

    /** A service principal, which is how the other cvhome services reach billing. */
    public String service(String scope) {
        return tokens.s2s(scope);
    }

    // ------------------------------------------------------------------------------------------------------ http

    public static String scoped(String path, String store) {
        return ApiClient.scoped(path, store);
    }

    public static String path(Object... segments) {
        return ApiClient.path(segments);
    }

    public static JsonNode json(ResponseEntity<String> response) {
        return ApiClient.json(response);
    }

    public static void expect(ResponseEntity<String> response, HttpStatus status) {
        ApiClient.expect(response, status);
    }

    public ResponseEntity<String> get(String url, String token) {
        return client.get(url, token);
    }

    public ResponseEntity<String> post(String url, String token, String body) {
        return client.send(HttpMethod.POST, url, token, body);
    }

    public ResponseEntity<String> send(HttpMethod method, String url, String token, String body) {
        return client.send(method, url, token, body);
    }

}
