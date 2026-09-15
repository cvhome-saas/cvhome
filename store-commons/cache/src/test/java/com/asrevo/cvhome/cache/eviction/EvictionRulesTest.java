package com.asrevo.cvhome.cache.eviction;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.cache.TestRegions;
import com.asrevo.cvhome.cache.event.PriceChanged;
import com.asrevo.cvhome.cache.event.StockChanged;
import com.asrevo.cvhome.cache.event.StoreChanged;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EvictionRulesTest {

    private static final String PACKAGE = "com.asrevo.cvhome.cache.eviction";

    static final class Product {
    }

    static final class Category {
    }

    static final class Outbox {
    }

    @Test
    void aRuleNamesTheRegionsAWriteToAnEntityDrops() {
        EvictionRules rules = EvictionRules.in(PACKAGE)
                .on(Product.class).evict(TestRegions.PRODUCT, TestRegions.LISTING)
                .on(List.of(Category.class, Product.class)).evict(List.of(TestRegions.LISTING))
                .build();

        assertThat(rules.entityPackage()).isEqualTo(PACKAGE);
        assertThat(rules.regionsFor(Product.class)).containsExactlyInAnyOrder(TestRegions.PRODUCT, TestRegions.LISTING);
        assertThat(rules.regionsFor(Category.class)).containsExactly(TestRegions.LISTING);
        assertThat(rules.regionsFor(Outbox.class)).isEmpty();
        assertThat(rules.entities()).containsExactly(Product.class, Category.class);
        assertThat(rules.owns(Outbox.class.getName())).isTrue();
        assertThat(rules.owns("io.namastack.outbox.OutboxRecord")).isFalse();
        assertThat(rules.owns(null)).isFalse();
    }

    @Test
    void noRulesWatchNothingAndAMalformedRuleIsRefused() {
        EvictionRules none = EvictionRules.none();

        assertThat(none.owns(Product.class.getName())).isFalse();
        assertThat(none.regionsFor(Product.class)).isEmpty();
        assertThat(none.entityPackage()).isEmpty();
        assertThatThrownBy(() -> EvictionRules.in(" ")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> EvictionRules.in(PACKAGE).on()).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> EvictionRules.in(PACKAGE).on(String.class)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("java.lang.String");
        assertThatThrownBy(() -> EvictionRules.in(PACKAGE).on(Product.class).evict())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void anEventRuleNamesTheRegionsAReceivedEventDrops() {
        EvictionRules rules = EvictionRules.in(PACKAGE)
                .on(Product.class).evict(TestRegions.PRODUCT)
                .onEvent(StockChanged.class, PriceChanged.class).evict(TestRegions.PRODUCT)
                .onEvent(StockChanged.class).evict(List.of(TestRegions.LISTING))
                .build();

        assertThat(rules.regionsForEvent(StockChanged.class)).containsExactly(TestRegions.PRODUCT, TestRegions.LISTING);
        assertThat(rules.regionsForEvent(PriceChanged.class)).containsExactly(TestRegions.PRODUCT);
        assertThat(rules.regionsForEvent(StoreChanged.class)).isEmpty();
        assertThat(rules.events()).containsExactly(StockChanged.class, PriceChanged.class);
        assertThat(EvictionRules.none().regionsForEvent(StockChanged.class)).isEmpty();
        assertThatThrownBy(() -> EvictionRules.in(PACKAGE).onEvent()).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> EvictionRules.in(PACKAGE).onEvent(StockChanged.class).evict())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aServiceWithoutTablesOfItsOwnMapsEventsOnly() {
        EvictionRules rules = EvictionRules.onEvents().onEvent(StoreChanged.class).evict(TestRegions.PRODUCT).build();

        assertThat(rules.entityPackage()).isEmpty();
        assertThat(rules.owns(Product.class.getName())).isFalse();
        assertThat(rules.regionsForEvent(StoreChanged.class)).containsExactly(TestRegions.PRODUCT);
        assertThatThrownBy(() -> EvictionRules.onEvents().on(Product.class))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("EvictionRules.in");
    }
}
