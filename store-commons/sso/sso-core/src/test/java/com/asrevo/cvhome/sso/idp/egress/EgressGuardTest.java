package com.asrevo.cvhome.sso.idp.egress;

import java.time.Duration;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.commons.domain.RealmId;
import com.asrevo.cvhome.sso.realm.RealmContext;
import com.asrevo.cvhome.sso.realm.RealmMode;
import com.asrevo.cvhome.sso.realm.SsoRealmProperties;
import com.asrevo.cvhome.sso.realm.SsoTenantIdentifierResolver;
import com.asrevo.cvhome.uaa.errors.IdpEndpointRefusedException;
import com.asrevo.cvhome.uaa.errors.IdpTestThrottledException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The endpoints a merchant may point this server at.
 *
 * <p>
 * Every URL on an identity provider is fetched by the server itself — on save, on test, and on every sign-in
 * through it. Unbounded that is a request forger inside the network, and the interesting targets are all
 * addresses a public DNS name can be made to resolve to.
 * </p>
 */
class EgressGuardTest {

    private static final String FIELD = "issuerUri";

    private static final String HTTPS = "https";

    private static final String ALIAS = "google";

    private final EgressGuard strict = guard(new EgressPolicy(null, false, null, 0, 0));

    private static EgressGuard guard(EgressPolicy policy) {
        SsoRealmProperties realm = new SsoRealmProperties();
        realm.setMode(RealmMode.MULTI);
        return new EgressGuard(policy, new SsoTenantIdentifierResolver(realm));
    }

    /** The defaults are the whole control: a deployment that configures nothing must still be protected. */
    @Test
    void theDefaultPolicyIsTheStrictOne() {
        EgressPolicy defaults = new EgressPolicy(null, false, null, 0, 0);

        assertThat(defaults.schemes()).containsExactly(HTTPS);
        assertThat(defaults.allowPrivateAddresses()).isFalse();
        assertThat(defaults.timeout()).isEqualTo(Duration.ofSeconds(5));
        assertThat(defaults.testsPerRealmPerHour()).isEqualTo(30);
    }

    @Test
    void loopbackAndTheMetadataServiceAreRefused() {
        assertThatThrownBy(() -> strict.check(FIELD, "https://127.0.0.1/.well-known/openid-configuration"))
                .isInstanceOf(IdpEndpointRefusedException.class);
        assertThatThrownBy(() -> strict.check(FIELD, "https://localhost:8443/x"))
                .isInstanceOf(IdpEndpointRefusedException.class);
        assertThatThrownBy(() -> strict.check(FIELD, "https://169.254.169.254/latest/meta-data/"))
                .isInstanceOf(IdpEndpointRefusedException.class);
        assertThatThrownBy(() -> strict.check(FIELD, "https://10.0.0.5/x"))
                .isInstanceOf(IdpEndpointRefusedException.class);
        assertThatThrownBy(() -> strict.check(FIELD, "https://192.168.1.1/x"))
                .isInstanceOf(IdpEndpointRefusedException.class);
        assertThatThrownBy(() -> strict.check(FIELD, "https://[::1]/x"))
                .isInstanceOf(IdpEndpointRefusedException.class);
    }

    @Test
    void plainHttpAndCredentialsInTheUrlAreRefused() {
        assertThatThrownBy(() -> strict.check(FIELD, "http://accounts.google.com/x"))
                .isInstanceOf(IdpEndpointRefusedException.class);
        // https://provider.example@10.0.0.5/ is a request to 10.0.0.5 that reads as a request to provider.example.
        assertThatThrownBy(() -> strict.check(FIELD, "https://accounts.google.com@127.0.0.1/x"))
                .isInstanceOf(IdpEndpointRefusedException.class);
        assertThatThrownBy(() -> strict.check(FIELD, "not-a-url"))
                .isInstanceOf(IdpEndpointRefusedException.class);
    }

    /** A name that cannot be resolved has not been checked, so it is refused rather than allowed. */
    @Test
    void aNameThatDoesNotResolveIsRefused() {
        assertThatThrownBy(() -> strict.check(FIELD, "https://no-such-host.invalid/x"))
                .isInstanceOf(IdpEndpointRefusedException.class);
    }

    @Test
    void nothingToCheckIsNotAFailure() {
        assertThatCode(() -> strict.check(FIELD, null)).doesNotThrowAnyException();
        assertThatCode(() -> strict.check(FIELD, "  ")).doesNotThrowAnyException();
    }

    /**
     * The local stack and the integration tests point providers at a stub on localhost, so the escape hatch has
     * to exist. It is off unless a deployment says otherwise, and this is the test that says what saying so does.
     */
    @Test
    void aDeploymentMayAllowItsOwnNetworkDeliberately() {
        EgressGuard relaxed = guard(new EgressPolicy(Set.of("http", HTTPS), true, null, 0, 0));

        assertThatCode(() -> relaxed.check(FIELD, "http://127.0.0.1:9999/x")).doesNotThrowAnyException();
    }

    @Test
    void theTestBudgetIsSpentPerRealm() {
        EgressGuard limited = guard(new EgressPolicy(null, false, null, 0, 2));
        RealmId storeA = RealmId.of("65f023632bc46470c104b76f");
        RealmId storeB = RealmId.of("65f023632bc46470c104b75f");

        RealmContext.runIn(storeA, () -> {
            assertThatCode(() -> limited.takeTestBudget(ALIAS)).doesNotThrowAnyException();
            assertThatCode(() -> limited.takeTestBudget(ALIAS)).doesNotThrowAnyException();
            assertThatThrownBy(() -> limited.takeTestBudget(ALIAS)).isInstanceOf(IdpTestThrottledException.class);
        });

        RealmContext.runIn(storeB, () ->
                assertThatCode(() -> limited.takeTestBudget(ALIAS))
                        .as("one store's spending is not another's").doesNotThrowAnyException());
    }

}
