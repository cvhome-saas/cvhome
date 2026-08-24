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

    public static final String SCOPE_STORE_POD = "store_pod";

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
        claims.put("sub", String.format("%s@%s", role.toLowerCase(), store));
        claims.put("name", String.format("Test %s", role));
        claims.put("roles", List.of(role));
        claims.put("scope", SCOPE_STORE_POD);
        claims.put("org", org);
        claims.put("store", store);
        claims.put("exp", Instant.now().plusSeconds(ONE_HOUR).getEpochSecond());
        return signer.sign(claims);
    }

    /**
     * Arbitrary claims, for the negative cases (missing store, expired, wrong scope).
     */
    public String custom(Map<String, Object> claims) {
        return signer.sign(claims);
    }

}
