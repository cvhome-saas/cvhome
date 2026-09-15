package com.asrevo.cvhome.aot;

import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import org.springframework.beans.factory.aot.AotServices;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MVC can build a query-parameter object natively. Without it catalog's search answered 500: "No primary or single
 * unique constructor found for class ProductSearchCriteria".
 */
class ControllerParameterRuntimeHintsTest {

    @Test
    void aModelAttributeOfOurOwnIsConstructibleAndItsSettersCallable() throws NoSuchMethodException {
        RuntimeHints hints = new RuntimeHints();

        new ControllerParameterRuntimeHints().registerHints(hints, getClass().getClassLoader());

        assertThat(RuntimeHintsPredicates.reflection().onType(StubCriteria.class)
                .withMemberCategory(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)).accepts(hints);
        assertThat(RuntimeHintsPredicates.reflection()
                .onMethodInvocation(StubCriteria.class.getMethod("setQuery", String.class))).accepts(hints);
    }

    @Test
    void theRegistrarIsDiscoveredByAotProcessingWithoutABean() {
        assertThat(AotServices.factories().load(RuntimeHintsRegistrar.class))
                .anyMatch(ControllerParameterRuntimeHints.class::isInstance);
    }

    /** Stands in for catalog's ProductSearchCriteria: bound from the query string, not a body. */
    public static class StubCriteria {

        private String query;

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

    }

    @RestController
    static class StubApi {

        @GetMapping("/search")
        String search(StubCriteria criteria) {
            return criteria.getQuery();
        }

    }

}
