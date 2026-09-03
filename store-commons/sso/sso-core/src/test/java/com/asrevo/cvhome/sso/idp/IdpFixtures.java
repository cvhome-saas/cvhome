package com.asrevo.cvhome.sso.idp;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.asrevo.cvhome.sso.domain.AccountLinking;
import com.asrevo.cvhome.sso.domain.IdentityProvider;
import com.asrevo.cvhome.sso.dto.IdentityProviderRequest;

/** Providers the idp tests share. */
final class IdpFixtures {

    static final String ALIAS = "corp";

    static final String CLIENT_ID = "client-1";

    static final String SECRET = "s3cret";

    static final String ISSUER = "https://idp.example";

    private static final String CORP = "Corp";

    private static final String USER_ROLE = "USER";

    private IdpFixtures() {
    }

    static IdentityProviderRequest request(IdpPreset preset, AccountLinking linking, boolean jit) {
        return new IdentityProviderRequest(ALIAS, CORP, preset, false, CLIENT_ID, SECRET, ISSUER,
                String.format("%s/authorize", ISSUER), String.format("%s/token", ISSUER), String.format("%s/userinfo", ISSUER),
                String.format("%s/jwks", ISSUER), List.of("openid", "email"), null, null, List.of("example.com"), linking, jit,
                List.of(USER_ROLE), true, Map.of());
    }

    static IdentityProvider provider(AccountLinking linking, boolean jit, boolean trustEmail) {
        IdentityProvider p = new IdentityProvider();
        p.setId(UUID.randomUUID());
        p.setAlias(ALIAS);
        p.setDisplayName(CORP);
        p.setPreset(IdpPreset.GENERIC_OIDC);
        p.setType(IdpPreset.GENERIC_OIDC.type());
        p.setAccountLinking(linking);
        p.setJitProvisioning(jit);
        p.setTrustEmailVerified(trustEmail);
        p.setDefaultRoles(USER_ROLE);
        p.setAttributeMapping("email=email,given_name=firstName,family_name=lastName");
        p.setEmailDomains("example.com,eng.example.com");
        p.setClientIdEnc("x");
        p.setCreatedAt(Instant.EPOCH);
        p.setUpdatedAt(Instant.EPOCH);
        return p;
    }

}
