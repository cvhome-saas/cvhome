package com.asrevo.cvhome.cache;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.commons.domain.ProductId;
import com.asrevo.cvhome.commons.domain.Sku;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.commons.domain.VariantId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * A key names its store first, then its language, then typed parts; it renders as one string; and it refuses at
 * the call whatever would make it say nothing about what it identifies.
 */
class CacheKeyTest {

    private static final String EN = "en";

    private static final String NONE = "-";

    private static final Sku SKU = Sku.of("SKU-1");

    private static final String SHOES = "shoes";

    private static final String ALL = "all";

    enum Kind {
        HOME
    }

    /** {@code storeA|lang|part|part}. */
    private static String expected(String language, String... parts) {
        StringBuilder text = new StringBuilder(Stores.A.getId()).append('|').append(language);
        for (String part : parts) {
            text.append('|').append(part);
        }
        return text.toString();
    }

    @Test
    void theFactoriesRenderStoreLanguageThenParts() {
        assertThat(CacheKey.of(Stores.A).render()).isEqualTo(expected(NONE));
        assertThat(CacheKey.of(Stores.A, Stores.EN).render()).isEqualTo(expected(EN));
        assertThat(CacheKey.sku(Stores.A, SKU).render()).isEqualTo(expected(NONE, SKU.value()));
        assertThat(CacheKey.sku(Stores.A, Stores.ES, SKU).render()).isEqualTo(expected("es", SKU.value()));
        assertThat(CacheKey.product(Stores.A, Stores.EN, ProductId.of(7)).render()).isEqualTo(expected(EN, "7"));
        assertThat(CacheKey.variant(Stores.A, Stores.EN, VariantId.of(9)).render()).isEqualTo(expected(EN, "9"));
        assertThat(CacheKey.slug(Stores.A, Stores.EN, SHOES).render()).isEqualTo(expected(EN, SHOES));
        QueryHash hash = QueryHash.of("q=shoes");
        assertThat(CacheKey.query(Stores.A, Stores.EN, hash).render()).isEqualTo(expected(EN, hash.value()));
        assertThat(CacheKey.of(Stores.A, Stores.EN).with(Kind.HOME, 3, ALL).render())
                .isEqualTo(expected(EN, "HOME", "3", ALL));
        assertThat(CacheKey.of(Stores.A, Stores.EN)).hasToString(expected(EN));
    }

    @Test
    void aGlobalKeyBelongsToTheSentinelStore() {
        CacheKey key = CacheKey.global(Stores.EN);

        assertThat(key.isGlobal()).isTrue();
        assertThat(key.store()).isEqualTo(CacheKey.GLOBAL);
        assertThat(key.render()).isEqualTo("*|en");
        assertThat(CacheKey.of(Stores.A).isGlobal()).isFalse();
    }

    @Test
    void keysAreValuesAndWithLeavesTheOriginalAlone() {
        CacheKey key = CacheKey.of(Stores.A, Stores.EN);
        CacheKey more = key.with(SHOES);

        assertThat(key).isEqualTo(CacheKey.of(Stores.A, Stores.EN)).hasSameHashCodeAs(CacheKey.of(Stores.A, Stores.EN));
        assertThat(key.parts()).isEmpty();
        assertThat(more.parts()).containsExactly(SHOES);
        assertThat(more).isNotEqualTo(key).isNotEqualTo(CacheKey.of(Stores.B, Stores.EN).with(SHOES));
    }

    @Test
    void aPartThatSaysNothingIsRefusedByType() {
        assertThatThrownBy(() -> CacheKey.of(Stores.A).with(7L)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("java.lang.Long");
        assertThatThrownBy(() -> CacheKey.of(Stores.A).with(new Object())).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> CacheKey.of(Stores.A).with((Object) null)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null");
        assertThatThrownBy(() -> CacheKey.of(Stores.A).with("")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> CacheKey.of(Stores.A).with("a|b")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> CacheKey.of(Stores.A).with(1.5)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aKeyNamesItsStoreAndItsParts() {
        assertThatThrownBy(() -> CacheKey.of((StoreMerchantId) null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new CacheKey(Stores.A, Stores.EN, null)).isInstanceOf(IllegalArgumentException.class);
        assertThat(new CacheKey(Stores.A, null, List.of(SKU)).parts()).containsExactly(SKU);
    }
}
