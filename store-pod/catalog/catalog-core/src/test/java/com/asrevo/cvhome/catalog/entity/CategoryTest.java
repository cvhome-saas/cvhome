package com.asrevo.cvhome.catalog.entity;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.commons.domain.LanguageCode;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The materialised path. {@code lineage} and {@code depth} are what turn "the whole subtree" into one {@code like}
 * query, so every re-parenting has to recompute both — a node that keeps a stale path silently disappears from
 * every read that walks the tree.
 */
class CategoryTest {

    private static final LanguageCode EN = new LanguageCode("en");

    private static Category category(long id) {
        Category category = new Category();
        category.setId(id);
        return category;
    }

    @Test
    void aRootNodeSitsAtDepthZeroUnderTheSeparator() {
        Category root = category(1L);

        root.placeUnder(null);

        assertThat(root.getDepth()).isZero();
        assertThat(root.getLineage()).isEqualTo("/1/");
        assertThat(root.getParent()).isNull();
        assertThat(root.subtreePrefix()).isEqualTo(root.getLineage());
    }

    @Test
    void aChildTakesItsParentsPathAndOneMoreLevel() {
        Category root = category(1L);
        root.placeUnder(null);
        Category child = category(7L);
        Category grandChild = category(9L);

        child.placeUnder(root);
        grandChild.placeUnder(child);

        assertThat(child.getLineage()).isEqualTo("/1/7/");
        assertThat(child.getDepth()).isEqualTo(1);
        assertThat(grandChild.getLineage()).isEqualTo("/1/7/9/");
        assertThat(grandChild.getDepth()).isEqualTo(2);
        // every descendant's lineage starts with the ancestor's, which is the whole point of the column
        assertThat(grandChild.getLineage()).startsWith(root.subtreePrefix());
    }

    @Test
    void movingToTheRootDropsTheOldAncestry() {
        Category root = category(1L);
        root.placeUnder(null);
        Category child = category(7L);
        child.placeUnder(root);

        child.placeUnder(null);

        assertThat(child.getLineage()).isEqualTo("/7/");
        assertThat(child.getDepth()).isZero();
        assertThat(child.getParent()).isNull();
    }

    @Test
    void unsetFlagsReadAsTheirDefaults() {
        Category category = new Category();

        assertThat(category.isVisible()).isFalse();
        assertThat(category.isFeatured()).isFalse();
        assertThat(category.getSortOrder()).isZero();
        assertThat(category.getDepth()).isZero();
        assertThat(category.description(EN)).isEmpty();
    }

}
