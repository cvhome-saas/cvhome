package com.asrevo.cvhome.aot;

import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import org.springframework.beans.factory.aot.AotServices;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * What a native service may read from the classpath by name. A resource missing here is not a build failure: the
 * native service fails at start, or starts and silently seeds nothing.
 */
class SharedResourcesRuntimeHintsTest {

    private static RuntimeHints registered() {
        RuntimeHints hints = new RuntimeHints();
        new SharedResourcesRuntimeHints().registerHints(hints, SharedResourcesRuntimeHintsTest.class.getClassLoader());
        return hints;
    }

    @Test
    void everyConfigSliceAServiceImportsIsIncluded() {
        RuntimeHints hints = registered();

        assertThat(RuntimeHintsPredicates.resource().forResource("common-config.yml")).accepts(hints);
        assertThat(RuntimeHintsPredicates.resource().forResource("lcl-config.yml")).accepts(hints);
        assertThat(RuntimeHintsPredicates.resource().forResource("fargate-config.yml")).accepts(hints);
        assertThat(RuntimeHintsPredicates.resource().forResource("store-pod-fargate-config.yml")).accepts(hints);
        // A service's own import: billing's native start refused without it.
        assertThat(RuntimeHintsPredicates.resource().forResource("plan-catalog.yml")).accepts(hints);
    }

    @Test
    void schemaSeedAndTestStoreScriptsAreIncludedAtAnyDepth() {
        RuntimeHints hints = registered();

        assertThat(RuntimeHintsPredicates.resource().forResource("init-sql/schema.sql")).accepts(hints);
        assertThat(RuntimeHintsPredicates.resource().forResource("init-sql/data-test-stores.sql")).accepts(hints);
        assertThat(RuntimeHintsPredicates.resource()
                .forResource("init-sql/stores/65f023632bc46470c104b76f/01-store.sql")).accepts(hints);
    }

    @Test
    void theSlicePatternStaysAtTheRootInsteadOfSweepingInEveryYamlFile() {
        assertThat(RuntimeHintsPredicates.resource().forResource("static/some-config.yml")).rejects(registered());
    }

    @Test
    void theRegistrarIsDiscoveredByAotProcessingWithoutABean() {
        // Config data is read before the context exists, so aot.factories is the only way the hint reaches the build.
        assertThat(AotServices.factories().load(RuntimeHintsRegistrar.class))
                .anyMatch(SharedResourcesRuntimeHints.class::isInstance);
    }

}
