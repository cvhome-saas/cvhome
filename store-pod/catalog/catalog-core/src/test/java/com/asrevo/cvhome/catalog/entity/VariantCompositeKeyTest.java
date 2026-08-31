package com.asrevo.cvhome.catalog.entity;

import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The two composite keys the variant model is joined on.
 *
 * <p>
 * Worth testing on their own because a wrong {@code equals}/{@code hashCode} here does not fail loudly: it
 * makes Hibernate treat a row it already has as a new one, and the variant service merges in place precisely
 * so that inserts never race deletes on a reused key. A key that is unequal to its own twin turns every
 * whole-set replace into insert-then-violate-the-unique-constraint.
 * </p>
 */
class VariantCompositeKeyTest {

    @Test
    void aVariantOptionValueKeyEqualsAnIdenticalOneAndSharesItsHash() {
        ProductVariantOptionValue.Key key = new ProductVariantOptionValue.Key(7L, 3L);
        ProductVariantOptionValue.Key same = new ProductVariantOptionValue.Key(7L, 3L);

        assertThat(key).isEqualTo(same).hasSameHashCodeAs(same);
        // the identity branch, and the two ways it can differ
        assertThat(key).isEqualTo(key)
                .isNotEqualTo(new ProductVariantOptionValue.Key(8L, 3L))
                .isNotEqualTo(new ProductVariantOptionValue.Key(7L, 4L))
                .isNotEqualTo(new ProductOptionAssignment.Key(7L, 3L));

        // and therefore de-duplicates in a hash collection, which is what the entity's Set relies on
        assertThat(Set.of(key)).contains(same);
    }

    @Test
    void anEmptyVariantOptionValueKeyIsUsableBeforeItsIdsAreKnown() {
        // Hibernate instantiates the key before populating it; nulls must not throw.
        ProductVariantOptionValue.Key blank = new ProductVariantOptionValue.Key();

        assertThat(blank.getVariantId()).isNull();
        assertThat(blank.getOptionId()).isNull();
        assertThat(blank).isEqualTo(new ProductVariantOptionValue.Key())
                .isNotEqualTo(new ProductVariantOptionValue.Key(1L, 1L));
        assertThat(blank.hashCode()).isEqualTo(new ProductVariantOptionValue.Key().hashCode());
    }

    @Test
    void anAssignmentKeyEqualsAnIdenticalOneAndSharesItsHash() {
        ProductOptionAssignment.Key key = new ProductOptionAssignment.Key(5L, 2L);
        ProductOptionAssignment.Key same = new ProductOptionAssignment.Key(5L, 2L);

        assertThat(key).isEqualTo(same).hasSameHashCodeAs(same);
        assertThat(key).isEqualTo(key)
                .isNotEqualTo(new ProductOptionAssignment.Key(6L, 2L))
                .isNotEqualTo(new ProductOptionAssignment.Key(5L, 3L))
                .isNotEqualTo("not a key");

        assertThat(Set.of(key)).contains(same);
    }

    @Test
    void anEmptyAssignmentKeyIsUsableBeforeItsIdsAreKnown() {
        ProductOptionAssignment.Key blank = new ProductOptionAssignment.Key();

        assertThat(blank.getProductId()).isNull();
        assertThat(blank.getOptionId()).isNull();
        assertThat(blank).isEqualTo(new ProductOptionAssignment.Key());
        assertThat(blank.hashCode()).isEqualTo(new ProductOptionAssignment.Key().hashCode());
    }
}
