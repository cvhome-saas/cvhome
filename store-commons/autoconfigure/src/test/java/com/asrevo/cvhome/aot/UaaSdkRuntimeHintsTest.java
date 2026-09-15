package com.asrevo.cvhome.aot;

import java.net.URL;
import java.net.URLClassLoader;

import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import org.springframework.beans.factory.aot.AotServices;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A native tenancy can call uaa through the admin SDK. Its first call exited the service before this: "Record
 * components not available for record class OAuth2TokenManager$TokenResponse".
 */
class UaaSdkRuntimeHintsTest {

    @Test
    void aServiceWithoutTheSdkGetsNothing() {
        RuntimeHints hints = new RuntimeHints();

        new UaaSdkRuntimeHints().registerHints(hints, new URLClassLoader(new URL[0], null));

        assertThat(hints.reflection().typeHints()).isEmpty();
    }

    @Test
    void theSdksPrivateTokenRecordAndItsApiTypesAreBindable() {
        RuntimeHints hints = new RuntimeHints();

        new UaaSdkRuntimeHints().registerHints(hints, getClass().getClassLoader());

        assertThat(RuntimeHintsPredicates.reflection()
                .onType(TypeReference.of("com.asrevo.cvhome.uaa.sdk.OAuth2TokenManager$TokenResponse"))).accepts(hints);
        assertThat(RuntimeHintsPredicates.reflection()
                .onType(TypeReference.of("com.asrevo.cvhome.uaa.domain.user.UserCounts"))).accepts(hints);
    }

    @Test
    void theRegistrarIsDiscoveredByAotProcessingWithoutABean() {
        assertThat(AotServices.factories().load(RuntimeHintsRegistrar.class))
                .anyMatch(UaaSdkRuntimeHints.class::isInstance);
    }

}
