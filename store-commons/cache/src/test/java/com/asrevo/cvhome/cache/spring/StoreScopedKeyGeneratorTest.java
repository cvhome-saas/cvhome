package com.asrevo.cvhome.cache.spring;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.cache.CacheKey;
import com.asrevo.cvhome.cache.CacheRegions;
import com.asrevo.cvhome.cache.CacheRegistry;
import com.asrevo.cvhome.cache.Stores;
import com.asrevo.cvhome.cache.TestRegions;
import com.asrevo.cvhome.cache.config.CacheProperties;
import com.asrevo.cvhome.cache.provider.caffeine.CaffeineCacheProvider;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.ProductId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The key of a cached method comes from its typed parameters wherever they stand; a method that cannot be keyed
 * safely fails at its first call, with the method named.
 */
class StoreScopedKeyGeneratorTest {

    private static final String SHOES = "shoes";

    private static final String PRODUCT = "product";

    private final CacheRegistry registry = new CacheRegistry(CacheRegions.of(TestRegions.values()),
            List.of(new CaffeineCacheProvider()), CacheProperties.defaults());

    private final StoreScopedKeyGenerator keys = new StoreScopedKeyGenerator(registry);

    /** A shopper's id, as checkout declares one: refused by its name. */
    record ShopperId(String sub) {
    }

    @CacheConfig(cacheNames = "test.listing")
    static class Reads {

        @Cacheable("test.product")
        String product(LanguageCode language, String slug, StoreMerchantId store) {
            return slug;
        }

        @Cacheable("test.product")
        String paged(StoreMerchantId store, LanguageCode language, Pageable page) {
            return SHOES;
        }

        @Cacheable("test.country")
        String countries(LanguageCode language) {
            return SHOES;
        }

        @Cacheable("test.product")
        String noStore(LanguageCode language, String slug) {
            return slug;
        }

        @Cacheable("test.product")
        String rawId(StoreMerchantId store, Long id) {
            return SHOES;
        }

        @Cacheable("test.product")
        String cart(StoreMerchantId store, ShopperId shopper) {
            return SHOES;
        }

        String viaClass(StoreMerchantId store, ProductId product) {
            return SHOES;
        }
    }

    private static Method method(String name) {
        for (Method candidate : Reads.class.getDeclaredMethods()) {
            if (candidate.getName().equals(name)) {
                return candidate;
            }
        }
        throw new IllegalArgumentException(name);
    }

    @Test
    void storeAndLanguageAreFoundWhereverTheyStandAndTheRestBecomeParts() {
        Reads target = new Reads();

        CacheKey key = (CacheKey) keys.generate(target, method(PRODUCT), Stores.EN, SHOES, Stores.A);
        CacheKey paged = (CacheKey) keys.generate(target, method("paged"), Stores.A, Stores.EN, PageRequest.of(1, 5));
        CacheKey byClass = (CacheKey) keys.generate(target, method("viaClass"), Stores.A, ProductId.of(3));

        assertThat(key).isEqualTo(CacheKey.slug(Stores.A, Stores.EN, SHOES));
        assertThat(paged.render()).endsWith("|en|p1s5");
        assertThat(byClass).isEqualTo(CacheKey.of(Stores.A, null).with(ProductId.of(3)));
        CacheKey absent = (CacheKey) keys.generate(target, method(PRODUCT), Stores.EN, null, Stores.A);
        assertThat(absent).isEqualTo(CacheKey.of(Stores.A, Stores.EN).with(CacheKey.ABSENT));
        assertThat(absent.render()).endsWith("|en|-");
    }

    @Test
    void aGlobalRegionNeedsNoStore() {
        CacheKey key = (CacheKey) keys.generate(new Reads(), method("countries"), Stores.EN);

        assertThat(key).isEqualTo(CacheKey.global(Stores.EN));
        assertThat(key.isGlobal()).isTrue();
    }

    @Test
    void aReadThatCannotBeKeyedSafelyFailsWithTheMethodNamed() {
        Reads target = new Reads();

        assertThatThrownBy(() -> keys.generate(target, method("noStore"), Stores.EN, SHOES))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Reads.noStore");
        assertThatThrownBy(() -> keys.generate(target, method("rawId"), Stores.A, 7L))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Reads.rawId")
                .hasMessageContaining("java.lang.Long");
        assertThatThrownBy(() -> keys.generate(target, method("cart"), Stores.A, new ShopperId("s")))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("ShopperId");
    }
}
