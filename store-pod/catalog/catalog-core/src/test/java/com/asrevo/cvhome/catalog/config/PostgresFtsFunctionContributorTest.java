package com.asrevo.cvhome.catalog.config;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.query.sqm.function.SqmFunctionRegistry;
import org.hibernate.type.spi.TypeConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * The functions a {@code Specification} reaches Postgres text search through.
 *
 * <p>
 * Registered against a real {@link SqmFunctionRegistry} rather than a mock, so this asserts they actually
 * register — a name that fails to resolve surfaces as a query-time error in whichever endpoint happens to use
 * it first, which is a long way from here.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class PostgresFtsFunctionContributorTest {

    @Mock
    private FunctionContributions contributions;

    private SqmFunctionRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new SqmFunctionRegistry();
        when(contributions.getFunctionRegistry()).thenReturn(registry);
        when(contributions.getTypeConfiguration()).thenReturn(new TypeConfiguration());
        new PostgresFtsFunctionContributor().contributeFunctions(contributions);
    }

    @Test
    void theMatchAndRankFunctionsAreAvailable() {
        assertThat(registry.findFunctionDescriptor("fts_match")).isNotNull();
        assertThat(registry.findFunctionDescriptor("fts_rank")).isNotNull();
    }

    /**
     * The tsquery must never be a separate registered function.
     *
     * <p>
     * Hibernate has no type for {@code tsquery}, so an intermediate would have to be declared as something and
     * would be cast back to it on the way in. Cast to text, {@code tsvector @@ text} silently re-parses the
     * query with the *default* text search configuration instead of the store language's — English keeps
     * working because english is the default, and Arabic quietly matches nothing. Reintroducing a standalone
     * query function is how that bug comes back.
     * </p>
     */
    @Test
    void thereIsNoStandaloneQueryFunctionToBuildATsqueryWith() {
        assertThat(registry.findFunctionDescriptor("fts_query")).isNull();
        assertThat(registry.findFunctionDescriptor("fts_prefix_query")).isNull();
        assertThat(registry.findFunctionDescriptor("search_config")).isNull();
        assertThat(registry.findFunctionDescriptor("search_normalize")).isNull();
        // Trigram closeness is asked for in the native near-miss and suggest queries, never through Criteria.
        assertThat(registry.findFunctionDescriptor("trgm_similarity")).isNull();
    }
}
