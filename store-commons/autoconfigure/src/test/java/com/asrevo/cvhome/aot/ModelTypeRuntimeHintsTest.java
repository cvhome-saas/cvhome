package com.asrevo.cvhome.aot;

import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import org.springframework.beans.factory.aot.AotServices;

import com.asrevo.cvhome.aot.model.StoredMeta;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A native service can read and write the JSON no controller names. On the first native load-test run every storefront
 * banner read answered 500: "Record components not available for record class BannerMeta".
 */
class ModelTypeRuntimeHintsTest {

    private static RuntimeHints registered() {
        RuntimeHints hints = new RuntimeHints();
        new ModelTypeRuntimeHints().registerHints(hints, ModelTypeRuntimeHintsTest.class.getClassLoader());
        return hints;
    }

    @Test
    void aRecordInAModelPackageHasItsComponentsRegistered() throws NoSuchMethodException {
        RuntimeHints hints = registered();

        assertThat(RuntimeHintsPredicates.reflection().onType(StoredMeta.class)).accepts(hints);
        assertThat(RuntimeHintsPredicates.reflection().onMethodInvocation(StoredMeta.class.getMethod("target")))
                .accepts(hints);
    }

    @Test
    void aTypeOutsideAModelPackageIsLeftToTheRegistrarsThatKnowIt() {
        assertThat(ModelTypeRuntimeHints.modelTypes(getClass().getClassLoader()))
                .contains(StoredMeta.class)
                .doesNotContain(NotAModel.class);
    }

    @Test
    void theRegistrarIsDiscoveredByAotProcessingWithoutABean() {
        assertThat(AotServices.factories().load(RuntimeHintsRegistrar.class))
                .anyMatch(ModelTypeRuntimeHints.class::isInstance);
    }

    /** A record outside any model package. */
    public record NotAModel(String value) {
    }

}
