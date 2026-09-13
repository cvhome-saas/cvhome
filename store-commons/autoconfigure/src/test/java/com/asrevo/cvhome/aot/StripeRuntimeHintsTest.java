package com.asrevo.cvhome.aot;

import java.net.URL;
import java.net.URLClassLoader;

import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import org.springframework.beans.factory.aot.AotServices;

import com.stripe.param.ProductCreateParams;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A native billing or payment service can read what Stripe answers and send what it asks. Nothing here fails a build;
 * a missing hint fails the first Stripe call of a running service.
 */
class StripeRuntimeHintsTest {

    private static final ClassLoader LOADER = StripeRuntimeHintsTest.class.getClassLoader();

    private static RuntimeHints registered() {
        RuntimeHints hints = new RuntimeHints();
        new StripeRuntimeHints().registerHints(hints, LOADER);
        return hints;
    }

    private static boolean gsonCanFill(RuntimeHints hints, String type) {
        return RuntimeHintsPredicates.reflection().onType(TypeReference.of(type))
                .withMemberCategories(MemberCategory.DECLARED_FIELDS, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)
                .test(hints);
    }

    /**
     * A param our code builds — standing in for billing's catalog publisher, which is where the scan finds the real
     * ones. The reference in this class's constant pool is the whole point.
     */
    static ProductCreateParams aProductWeCreate() {
        return ProductCreateParams.builder().setName("Basic").build();
    }

    @Test
    void everyResponseModelIncludingNestedOnesAndTheirSdkSuperclassesIsFillable() {
        RuntimeHints hints = registered();

        assertThat(gsonCanFill(hints, "com.stripe.model.Event")).isTrue();
        assertThat(gsonCanFill(hints, "com.stripe.model.checkout.Session")).isTrue();
        assertThat(gsonCanFill(hints, "com.stripe.model.Subscription$PauseCollection")).isTrue();
        assertThat(gsonCanFill(hints, "com.stripe.net.ApiResource")).isTrue();
    }

    @Test
    void theParamsOurCodeBuildsAreFoundByReadingOurOwnBytecode() {
        assertThat(StripeRuntimeHints.referencedParams(LOADER)).contains(ProductCreateParams.class.getName());
    }

    @Test
    void aParamIsFollowedIntoWhatItCanHoldButNotIntoEveryOtherParam() {
        RuntimeHints hints = registered();

        assertThat(gsonCanFill(hints, ProductCreateParams.class.getName())).isTrue();
        assertThat(gsonCanFill(hints, ProductCreateParams.DefaultPriceData.class.getName())).isTrue();
        // Ten thousand param classes exist; registering one nobody builds is image size for nothing.
        assertThat(gsonCanFill(hints, "com.stripe.param.RefundCreateParams")).isFalse();
    }

    @Test
    void aServiceWithoutStripeGetsNothing() {
        RuntimeHints hints = new RuntimeHints();
        ClassLoader withoutStripe = new URLClassLoader(new URL[0], null);

        new StripeRuntimeHints().registerHints(hints, withoutStripe);

        assertThat(hints.reflection().typeHints()).isEmpty();
    }

    @Test
    void theRegistrarIsDiscoveredByAotProcessingWithoutABean() {
        assertThat(AotServices.factories().load(RuntimeHintsRegistrar.class))
                .anyMatch(StripeRuntimeHints.class::isInstance);
    }

}
