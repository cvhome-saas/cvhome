package com.asrevo.cvhome.cache.config;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.asrevo.cvhome.cache.CacheRegions;
import com.asrevo.cvhome.cache.CacheRegistry;
import com.asrevo.cvhome.cache.Stores;
import com.asrevo.cvhome.cache.TestRegions;
import com.asrevo.cvhome.cache.eviction.AfterCommitEviction;
import com.asrevo.cvhome.cache.eviction.CacheEvictionIntegrator;
import com.asrevo.cvhome.cache.eviction.CommitEvictionListener;
import com.asrevo.cvhome.cache.eviction.EvictionRules;
import com.asrevo.cvhome.cache.eviction.EvictionRulesValidator;
import com.asrevo.cvhome.cache.metrics.RegionCacheMeterBinderProvider;
import com.asrevo.cvhome.cache.spring.CvhomeCacheManager;
import com.asrevo.cvhome.cache.spring.StoreScopedKeyGenerator;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * With the module on the classpath a service caches by declaring its regions: a {@code @Cacheable} read is served
 * from its region on the second call, keyed by the store-scoped generator, and a name nobody declared stops the
 * start.
 */
class CacheAutoConfigurationTest {

    private static final String SHOES = "shoes";

    private static final String PRODUCT = "test.product";

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(CacheAutoConfiguration.class));

    /** A read as a service writes one. */
    static class ProductReads {

        private final AtomicInteger loads = new AtomicInteger();

        @Cacheable(cacheNames = "test.product", keyGenerator = StoreScopedKeyGenerator.BEAN)
        public String product(StoreMerchantId store, LanguageCode language, String slug) {
            loads.incrementAndGet();
            return slug;
        }

        int loads() {
            return loads.get();
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class Service {

        @Bean
        CacheRegions regions() {
            return CacheRegions.of(TestRegions.values());
        }

        @Bean
        ProductReads productReads() {
            return new ProductReads();
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class OwnManager {

        @Bean
        CacheManager cacheManager() {
            return new NoOpCacheManager();
        }
    }

    static class Typo {

        @Cacheable("test.produkt")
        public String product(StoreMerchantId store) {
            return SHOES;
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class WithTypo {

        @Bean
        Typo typo() {
            return new Typo();
        }
    }

    @Test
    void aServiceWithNoRegionsStillStartsWithAnEmptyManagerAndTheEvictionHookInstalled() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(CvhomeCacheManager.class).hasSingleBean(CacheRegistry.class)
                    .hasSingleBean(CommitEvictionListener.class).hasSingleBean(CacheEvictionIntegrator.class)
                    .hasSingleBean(AfterCommitEviction.class).hasSingleBean(EvictionRulesValidator.class)
                    .hasSingleBean(RegionCacheMeterBinderProvider.class);
            assertThat(context.getBean(CacheManager.class).getCacheNames()).isEmpty();
            assertThat(context.getBean(CacheRegions.class).regions()).isEmpty();
            assertThat(context.getBean(EvictionRules.class).entityPackage()).isEmpty();
            Map<String, Object> properties = new HashMap<>();
            context.getBean(HibernatePropertiesCustomizer.class).customize(properties);
            assertThat(properties).containsKey(CacheEvictionIntegrator.PROPERTY);
        });
    }

    @Test
    void aCacheableReadIsServedFromItsRegionOnTheSecondCall() {
        runner.withUserConfiguration(Service.class)
                .withPropertyValues("com.asrevo.cvhome.cache.regions.test.product.ttl=30s")
                .run(context -> {
                    ProductReads reads = context.getBean(ProductReads.class);

                    assertThat(reads.product(Stores.A, Stores.EN, SHOES)).isEqualTo(SHOES);
                    assertThat(reads.product(Stores.A, Stores.EN, SHOES)).isEqualTo(SHOES);
                    assertThat(reads.product(Stores.B, Stores.EN, SHOES)).isEqualTo(SHOES);

                    assertThat(reads.loads()).as("store A once, store B once").isEqualTo(2);
                    assertThat(context.getBean(CacheManager.class).getCacheNames()).contains(PRODUCT);
                    assertThat(context.getBean(CacheProperties.class).region(PRODUCT).ttl()).hasSeconds(30);
                });
    }

    @Test
    void anotherCacheManagerWinsAndATypoStopsTheStart() {
        runner.withUserConfiguration(OwnManager.class).run(context -> {
            assertThat(context).doesNotHaveBean(CvhomeCacheManager.class);
            assertThat(context.getBean(CacheManager.class)).isInstanceOf(NoOpCacheManager.class);
        });
        runner.withUserConfiguration(Service.class, WithTypo.class).run(context -> {
            assertThat(context).hasFailed();
            assertThat(context.getStartupFailure()).hasStackTraceContaining("test.produkt");
        });
    }
}
