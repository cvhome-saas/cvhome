package com.asrevo.cvhome.tenancy.support;

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
 * Real HTTP against tenancy, plus the tokens its endpoints actually distinguish between.
 *
 * <p>
 * Tenancy is org- and platform-scoped rather than store-scoped, so {@link Tokens#staff} alone is not enough. The
 * three shapes that matter here are the platform operator, an organization's administrator, and one store's
 * administrator. {@link #orgAdmin(String)} additionally carries a wildcard {@code store} claim, which is what the
 * console sends and what tenancy's own listing endpoints read; {@link Tokens#orgAdmin} leaves the claim out.
 * Both now mint the {@code store_pod} scope: an org admin carrying {@code store_core} would be read as
 * "platform-wide, no org" by {@code SecurityUtils.getOrgStoreIdentity}, which tests the scope before the role, and
 * every cross-organization assertion would pass without proving anything.
 * </p>
 */
public final class TenancyApiTestSupport {

    /** data.sql's first organization: owner of {@link Tokens#STORE_1} and {@link Tokens#STORE_2}. */
    public static final String ORG_A = "21f023932bc66470c104b76f";

    /** data.sql's second organization: owner of {@link Tokens#STORE_3} and {@link Tokens#STORE_4}. */
    public static final String ORG_B = "352023632b046970c104b76f";

    /** Renamed, suspended and resumed by the org lifecycle tests. See {@code data-test-stores.sql}. */
    public static final String ORG_LIFECYCLE = "11111111111111111111aa01";

    /** Already CLOSED, which is terminal. */
    public static final String ORG_CLOSED = "11111111111111111111aa02";

    /** The only organization with a recorded {@code owner_user_id}. */
    public static final String ORG_OWNED = "11111111111111111111aa03";

    public static final String ORG_OWNER_USER_ID = "b0a4f3d2-0000-4000-8000-000000000001";

    /** Carries a member and the invitations the member tests create. */
    public static final String ORG_MEMBERS = "11111111111111111111aa04";

    /** A second organization for the member tests, so isolation has something to be isolated from. */
    public static final String ORG_NEIGHBOUR = "11111111111111111111aa05";

    /** Starts with no stores, so a store created by a test is unambiguous. */
    public static final String ORG_CREATOR = "11111111111111111111aa06";

    /** SUSPENDED: its stores are refused entry even though their own rows are ACTIVE. */
    public static final String ORG_SUSPENDED = "11111111111111111111aa07";

    public static final String MEMBERS_STORE = "11111111111111111111bb01";

    public static final String SUSPENDED_ORG_STORE = "11111111111111111111bb02";

    public static final String SUSPENDED_STORE = "11111111111111111111bb03";

    public static final String DELETED_STORE = "11111111111111111111bb04";

    public static final String POD_ID = "507f1f77bcf86cd799439011";

    /** Every store on the {@code test-stores} seed sits on one pod, so this is the only pod a fixture names. */
    public static final String WILDCARD_STORE = "*";

    /** The property every value-object id is nested under when it is not a bare string. */
    public static final String ID_FIELD = "id";

    private static final long ONE_HOUR = 3600;

    private final ApiClient client;

    private final Tokens tokens;

    private final TestJwtSigner signer;

    public TenancyApiTestSupport(int port, TestJwtSigner signer) {
        this.client = new ApiClient(port);
        this.tokens = new Tokens(signer);
        this.signer = signer;
    }

    /** The platform operator: every organization, every store. */
    public String superAdmin() {
        return tokens.superAdmin();
    }

    /**
     * An organization's administrator as the console holds one: the {@code ORG_ADMIN} role, the {@code store_pod}
     * scope, and an {@code org} claim that is the only thing confining the rows they see.
     */
    public String orgAdmin(String org) {
        return withScope(Tokens.ROLE_ORG_ADMIN, org, WILDCARD_STORE);
    }

    /** One store's administrator inside {@code org}. */
    public String storeAdmin(String org, String store) {
        return tokens.staff(Tokens.ROLE_STORE_ADMIN, store, org);
    }

    public String storeModerator(String org, String store) {
        return tokens.staff(Tokens.ROLE_STORE_MODERATOR, store, org);
    }

    /** A signed-in principal holding none of the roles any tenancy endpoint admits. */
    public String customer(String org, String store) {
        return withScope("ROLE_CUSTOMER", org, store);
    }

    /** A service-to-service principal on the {@code store_core} scope: platform-wide, no organization. */
    public String service() {
        return tokens.s2s(Tokens.SCOPE_STORE_CORE);
    }

    private String withScope(String role, String org, String store) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", String.format("%s@%s", role.toLowerCase(), org));
        claims.put("name", String.format("Test %s", role));
        claims.put("roles", List.of(role));
        claims.put("scope", Tokens.SCOPE_STORE_POD);
        claims.put("org", org);
        claims.put("store", store);
        claims.put("exp", java.time.Instant.now().plusSeconds(ONE_HOUR).getEpochSecond());
        return signer.sign(claims);
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

    public static void expect(ResponseEntity<String> response, HttpStatus status) {
        ApiClient.expect(response, status);
    }

    public static JsonNode json(ResponseEntity<String> response) {
        return ApiClient.json(response);
    }

    public static String query(String path, String query) {
        return ApiClient.query(path, query);
    }

    public static String path(Object... segments) {
        return ApiClient.path(segments);
    }

    /** One query parameter, formatted rather than concatenated. */
    public static String param(String name, Object value) {
        return String.format("%s=%s", name, value);
    }

    /** {@code path?name=value}. */
    public static String with(String path, String name, Object value) {
        return ApiClient.query(path, param(name, value));
    }

    public static String slug(String prefix) {
        return ApiClient.slug(prefix);
    }

    /**
     * The bare id under {@code field}, whichever of its two wire shapes it arrived in.
     *
     * <p>
     * {@code StoreMerchantId} carries {@code @JsonValue} and serializes as a bare string; {@code ManagerOrgId} and
     * {@code PodId} still serialize as {@code {"id": "…"}}. Reading either through one helper keeps the assertions
     * about the row rather than about which value object it happens to hold.
     * </p>
     */
    public static String idOf(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value != null && value.isObject() ? value.get(ID_FIELD).asString() : value.asString();
    }

    /** The ids in a Spring Data {@code Page} body, in order. */
    public static List<String> idsOf(JsonNode page) {
        return page.get("content").valueStream().map(it -> idOf(it, ID_FIELD)).toList();
    }

}
