package com.asrevo.cvhome.aot;

import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import org.springframework.beans.factory.aot.AotServices;

import com.asrevo.cvhome.commons.domain.Identifier;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.PodId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A native service can build a value object from configuration, a path variable or JSON. Without it every pod service
 * refused to start: "Failed to bind properties under 'com.asrevo.cvhome.pods[0].id' to PodId".
 */
class ValueObjectRuntimeHintsTest {

    private static RuntimeHints registered() {
        RuntimeHints hints = new RuntimeHints();
        new ValueObjectRuntimeHints().registerHints(hints, ValueObjectRuntimeHintsTest.class.getClassLoader());
        return hints;
    }

    @Test
    void theStringConstructorOfAnIdentifierMayBeInvoked() throws NoSuchMethodException {
        assertThat(RuntimeHintsPredicates.reflection().onConstructorInvocation(PodId.class.getConstructor(String.class)))
                .accepts(registered());
    }

    @Test
    void identifiersOutsideTheDomainPackageAndOtherDomainValueObjectsAreCovered() {
        RuntimeHints hints = registered();

        assertThat(RuntimeHintsPredicates.reflection().onType(ElsewhereId.class)
                .withMemberCategory(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)).accepts(hints);
        assertThat(RuntimeHintsPredicates.reflection().onType(LanguageCode.class)
                .withMemberCategory(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)).accepts(hints);
    }

    @Test
    void theRegistrarIsDiscoveredByAotProcessingWithoutABean() {
        assertThat(AotServices.factories().load(RuntimeHintsRegistrar.class))
                .anyMatch(ValueObjectRuntimeHints.class::isInstance);
    }

    /** Stands in for billing-commons' PlanId: an Identifier that lives in its domain's own module. */
    public record ElsewhereId(String id) implements Identifier {

        @Override
        public Object getId() {
            return id;
        }

    }

}
