package com.asrevo.cvhome.catalog.config;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.boot.model.FunctionContributor;
import org.hibernate.type.BasicTypeReference;
import org.hibernate.type.StandardBasicTypes;

/**
 * Teaches Hibernate the Postgres text-search operators the product search needs, so they can be reached from a
 * Criteria {@code Specification} rather than forcing the whole query into native SQL.
 *
 * <p>
 * Registered through {@code META-INF/services/org.hibernate.boot.model.FunctionContributor}.
 * </p>
 */
public class PostgresFtsFunctionContributor implements FunctionContributor {

    private static final BasicTypeReference<Boolean> BOOLEAN = StandardBasicTypes.BOOLEAN;

    private static final BasicTypeReference<Float> FLOAT = StandardBasicTypes.FLOAT;

    private static final BasicTypeReference<String> STRING = StandardBasicTypes.STRING;

    @Override
    public void contributeFunctions(FunctionContributions contributions) {
        var registry = contributions.getFunctionRegistry();
        var types = contributions.getTypeConfiguration().getBasicTypeRegistry();

        // Each of these builds its tsquery inline, from the document, a language and the raw query text.
        //
        // The tempting shape — a separate `fts_query` function composed into `fts_match` — does not work, and
        // does not fail either. Hibernate has no type for `tsquery`, so the intermediate has to be declared as
        // something, and whatever it is declared as it gets cast back to that on the way in. Cast to text, the
        // `tsvector @@ text` operator silently re-parses the query with the *default* text search configuration
        // instead of the store language's. English keeps working, because english is the default; Arabic quietly
        // matches nothing. So the tsquery is never a value here — it only ever exists inside one SQL expression.

        // The match. An operator pattern rather than a wrapper function, so the GIN index is reachable; a
        // function around it would make Postgres evaluate it per row.
        //
        // websearch_to_tsquery, not to_tsquery: it gives shoppers quoted phrases and -exclusion for free, and
        // cannot be made to throw by whatever they paste in.
        registry.patternDescriptorBuilder("fts_match",
                        "(?1 @@ websearch_to_tsquery(catalog.search_config(?2), catalog.search_normalize(?3)))")
                .setExactArgumentCount(3)
                .setInvariantType(types.resolve(BOOLEAN))
                .register();

        // Cover density ranking: rewards hits that sit close together, which reads better than plain ts_rank for
        // multi-word product names.
        registry.patternDescriptorBuilder("fts_rank",
                        "ts_rank_cd(?1, websearch_to_tsquery(catalog.search_config(?2), catalog.search_normalize(?3)))")
                .setExactArgumentCount(3)
                .setInvariantType(types.resolve(FLOAT))
                .register();

        // Trigram closeness of a product name to what was typed, normalised on both sides by the same function
        // the index was built with. word_similarity compares the query against the best-matching run of words
        // inside the name; plain similarity would compare it against the whole name, so a two-word query against
        // a five-word product name scores near zero however well it actually matches.
        registry.patternDescriptorBuilder("trgm_similarity",
                        "public.word_similarity(catalog.search_normalize(?2), ?1)")
                .setExactArgumentCount(2)
                .setInvariantType(types.resolve(FLOAT))
                .register();
    }
}
