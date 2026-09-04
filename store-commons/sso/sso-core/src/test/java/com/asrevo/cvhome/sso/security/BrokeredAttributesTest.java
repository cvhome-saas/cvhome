package com.asrevo.cvhome.sso.security;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.sso.domain.AccountLinking;
import com.asrevo.cvhome.sso.domain.IdentityProvider;
import com.asrevo.cvhome.sso.idp.BrokeredIdentity;

import static org.assertj.core.api.Assertions.assertThat;

/** The mapping decides what is email and what is a name; a single "name" is split once. */
class BrokeredAttributesTest {

    private static final String EMAIL = "email";

    private static final String ADA = "Ada";

    private static final String LOVELACE = "Lovelace";

    private static final String SUB = "abc";

    private static final String MAIL = "ada@example.com";

    private static IdentityProvider provider(String mapping) {
        IdentityProvider p = new IdentityProvider();
        p.setAccountLinking(AccountLinking.CONFIRM);
        p.setAttributeMapping(mapping);
        return p;
    }

    @Test
    void mapsClaimsAndReadsVerification() {
        BrokeredIdentity identity = BrokeredAttributes.extract(provider("email=email,given_name=firstName,family_name=lastName"),
                SUB, Map.of(EMAIL, MAIL, "email_verified", true, "given_name", ADA, "family_name", LOVELACE));

        assertThat(identity.subject()).isEqualTo(SUB);
        assertThat(identity.email()).isEqualTo(MAIL);
        assertThat(identity.emailVerified()).isTrue();
        assertThat(identity.firstName()).isEqualTo(ADA);
        assertThat(identity.lastName()).isEqualTo(LOVELACE);
    }

    @Test
    void splitsASingleNameAndTreatsAMissingFlagAsUnverified() {
        BrokeredIdentity identity = BrokeredAttributes.extract(provider("email=email,name=firstName"), SUB,
                Map.of(EMAIL, MAIL, "name", "Ada Lovelace"));

        assertThat(identity.firstName()).isEqualTo(ADA);
        assertThat(identity.lastName()).isEqualTo(LOVELACE);
        assertThat(identity.emailVerified()).isFalse();
    }

}
