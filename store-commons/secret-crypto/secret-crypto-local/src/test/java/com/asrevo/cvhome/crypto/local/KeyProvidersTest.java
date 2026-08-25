package com.asrevo.cvhome.crypto.local;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The small key providers: static, callback and random.
 *
 * <p>
 * {@link RandomKeyProvider} is the one that matters, because the autoconfiguration falls back to it when no key can
 * be found. It must hold one key for the life of the instance — a provider that minted a fresh key per call would
 * encrypt and then fail to read back within a single process.
 * </p>
 */
class KeyProvidersTest {

    private static final byte[] KEY = "0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8);

    private static final String ENV_VAR = "COM_ASREVO_CVHOME_CRYPTO_KEY";

    @Test
    void aStaticProviderHandsBackTheKeyItWasGiven() {
        assertThat(new StaticKeyProvider(KEY).getKey()).hasValue(KEY);
    }

    @Test
    void aStaticProviderWithoutAKeyReadsAsAbsent() {
        assertThat(new StaticKeyProvider(null).getKey()).isEmpty();
    }

    @Test
    void aCallbackProviderAsksItsSupplierEachTime() {
        assertThat(new CustomCallbackKeyProvider(() -> KEY).getKey()).hasValue(KEY);
    }

    @Test
    void aCallbackProviderReturningNothingReadsAsAbsent() {
        assertThat(new CustomCallbackKeyProvider(() -> null).getKey()).isEmpty();
    }

    @Test
    void aRandomProviderKeepsOneKeyForTheLifeOfTheInstance() {
        RandomKeyProvider provider = new RandomKeyProvider();

        assertThat(provider.getKey()).hasValue(provider.getKey().orElseThrow());
        assertThat(provider.getKey().orElseThrow()).hasSize(32);
    }

    @Test
    void twoRandomProvidersDoNotAgreeOnAKey() {
        assertThat(new RandomKeyProvider().getKey().orElseThrow())
                .isNotEqualTo(new RandomKeyProvider().getKey().orElseThrow());
    }

    @Test
    void aRandomProviderHandedAnExplicitKeyUsesIt() {
        assertThat(new RandomKeyProvider(KEY).getKey()).hasValue(KEY);
    }

    @Test
    void aRandomProviderHandedNoKeyReadsAsAbsent() {
        assertThat(new RandomKeyProvider(null).getKey()).isEmpty();
    }

    /**
     * The env-var provider reads {@code System.getenv} directly, which Mockito refuses to mock, so the variable is
     * set for this module's test JVM in build.gradle instead. That covers the path a deployment actually takes; the
     * unset, blank and malformed branches have no seam to reach them from and stay uncovered deliberately.
     */
    @Test
    void theEnvironmentProviderDecodesTheKeyTheDeploymentSets() {
        assertThat(new EnvironmentVariableKeyProvider().getKey()).hasValue(KEY);
    }
}
