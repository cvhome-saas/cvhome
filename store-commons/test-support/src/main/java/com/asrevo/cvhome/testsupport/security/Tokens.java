package com.asrevo.cvhome.testsupport.security;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Claim shapes the platform's resource servers expect, plus the ids seeded by the {@code test-stores} profile.
 * Every store-scoped integration test needs a principal on the store under test and one on another store — that
 * second token is how tenant isolation is proven rather than assumed.
 */
public final class Tokens {

    public static final String ORG_1 = "32a034a43cd77581d105c87a";

    public static final String ROLE_STORE_ADMIN = "ROLE_STORE_ADMIN";

    public static final String ROLE_STORE_MODERATOR = "ROLE_STORE_MODERATOR";

    public static final String ROLE_SUPER_ADMIN = "ROLE_SUPER_ADMIN";

    public static final String ROLE_ORG_ADMIN = "ROLE_ORG_ADMIN";

    public static final String SCOPE_STORE_POD = "store_pod";

    public static final String SCOPE_STORE_CORE = "store_core";

    /** Stores the {@code test-stores} profile seeds (see each service's {@code init-sql/data-test-stores.sql}). */
    public static final String STORE_1 = "65f023632bc46470c104b76f";

    public static final String STORE_2 = "65f023632bc46470c104b75f";

    public static final String STORE_3 = "65f023632bc26470c104b75f";

    public static final String STORE_4 = "65f020632bc46470c104b76f";

    /** Claim names. Named because checkstyle counts a repeated literal, and because a typo in one of these
     * mints a token the resource server silently reads as anonymous rather than rejecting. */
    private static final String SUB = "sub";

    private static final String NAME = "name";

    private static final String ROLES = "roles";

    private static final String SCOPE = "scope";

    private static final String ORG = "org";

    private static final String STORE = "store";

    private static final String EXP = "exp";

    private static final long ONE_HOUR = 3600;

    private final TestJwtSigner signer;

    public Tokens(TestJwtSigner signer) {
        this.signer = signer;
    }

    /**
     * A staff token for {@code role} on {@code store} in {@link #ORG_1}.
     */
    public String staff(String role, String store) {
        return staff(role, store, ORG_1);
    }

    public String staff(String role, String store, String org) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(SUB, String.format("%s@%s", role.toLowerCase(), store));
        claims.put(NAME, String.format("Test %s", role));
        claims.put(ROLES, List.of(role));
        claims.put(SCOPE, SCOPE_STORE_POD);
        claims.put(ORG, org);
        claims.put(STORE, store);
        claims.put(EXP, Instant.now().plusSeconds(ONE_HOUR).getEpochSecond());
        return signer.sign(claims);
    }

    /**
     * The platform operator: {@code ROLE_SUPER_ADMIN}, every store, no org.
     */
    public String superAdmin() {
        Map<String, Object> claims = base("super-admin@platform", "Test Super Admin", List.of(ROLE_SUPER_ADMIN), SCOPE_STORE_CORE);
        return signer.sign(claims);
    }

    /**
     * An org administrator of {@code org}: sees every store the org owns.
     */
    public String orgAdmin(String org) {
        Map<String, Object> claims = base(String.format("org-admin@%s", org), "Test Org Admin", List.of(ROLE_ORG_ADMIN), SCOPE_STORE_CORE);
        claims.put(ORG, org);
        return signer.sign(claims);
    }

    /**
     * A service-to-service client-credentials token carrying {@code scope} ({@link #SCOPE_STORE_CORE} or
     * {@link #SCOPE_STORE_POD}); {@code resource} names the pod for pod-scoped calls.
     */
    public String s2s(String scope, String resource) {
        Map<String, Object> claims = base(String.format("s2s-%s", scope), String.format("Test S2S %s", scope), List.of(), scope);
        if (resource != null) {
            claims.put("resource", resource);
        }
        return signer.sign(claims);
    }

    public String s2s(String scope) {
        return s2s(scope, null);
    }

    private static Map<String, Object> base(String sub, String name, List<String> roles, String scope) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(SUB, sub);
        claims.put(NAME, name);
        claims.put(ROLES, roles);
        claims.put(SCOPE, scope);
        claims.put(EXP, Instant.now().plusSeconds(ONE_HOUR).getEpochSecond());
        return claims;
    }

    /**
     * Arbitrary claims, for the negative cases (missing store, expired, wrong scope).
     */
    public String custom(Map<String, Object> claims) {
        return signer.sign(claims);
    }

}
