package com.asrevo.cvhome.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.support.TransactionTemplate;

import com.asrevo.cvhome.cache.eviction.AfterCommitEviction;
import com.asrevo.cvhome.cache.eviction.EvictionRules;
import com.asrevo.cvhome.commons.domain.CategoryId;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.ManufacturerId;
import com.asrevo.cvhome.commons.domain.ProductId;
import com.asrevo.cvhome.commons.domain.Sku;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.commons.domain.VariantId;
import com.asrevo.cvhome.testsupport.containers.PostgresTestConfiguration;
import com.asrevo.cvhome.testsupport.sql.SqlStatements;

import io.micrometer.core.instrument.MeterRegistry;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The library on a real Postgres and a real Hibernate: a cached read costs no statement the second time, a
 * committed write to a store's entity makes that store's next read pay and leaves the other store's warm, a bulk
 * write evicts after its commit, and the region reads on the meters.
 */
@SpringBootTest(classes = CacheIntegrationTest.App.class, properties = {SqlStatements.INSPECTOR,
    "spring.jpa.hibernate.ddl-auto=create-drop", "spring.jpa.open-in-view=false",
    "com.asrevo.cvhome.cache.regions.shop.name.ttl=30s", "com.asrevo.cvhome.cache.regions.shop.search.enabled=false"})
@Import(PostgresTestConfiguration.class)
class CacheIntegrationTest {

    private static final StoreMerchantId A = new StoreMerchantId("65f023632bc46470c104b75f");

    private static final StoreMerchantId B = new StoreMerchantId("65f020632bc46470c104b76f");

    private static final LanguageCode EN = LanguageCode.defaultLanguage();

    private static final String SHOES = "Shoes";

    private static final String BOOTS = "Boots";

    private static final String CACHE = "cache";

    @Autowired
    private ShopRepository shops;

    @Autowired
    private ShopReads reads;

    @Autowired
    private CacheRegistry registry;

    @Autowired
    private AfterCommitEviction afterCommit;

    @Autowired
    private TransactionTemplate transactions;

    @Autowired
    private MeterRegistry meters;

    @Autowired
    private CacheManager cacheManager;

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EntityScan(basePackageClasses = Shop.class)
    @EnableJpaRepositories(basePackageClasses = ShopRepository.class)
    static class App {

        @Bean
        CacheRegions shopRegions() {
            return CacheRegions.of(ShopRegions.values());
        }

        @Bean
        EvictionRules shopEvictionRules() {
            return EvictionRules.in("com.asrevo.cvhome.cache").on(Shop.class).evict(ShopRegions.NAME).build();
        }

        @Bean
        ShopReads shopReads(ShopRepository shops) {
            return new ShopReads(shops);
        }
    }

    @BeforeEach
    void seed() {
        shops.deleteAll();
        shops.saveAll(List.of(new Shop(A, SHOES), new Shop(B, SHOES)));
        registry.region(ShopRegions.NAME, String.class).clear();
    }

    @Test
    void aSecondReadCostsNoStatementAndAWriteEvictsItsStoreAlone() throws Exception {
        assertThat(reads.name(A, EN)).isEqualTo(SHOES);
        assertThat(reads.name(B, EN)).isEqualTo(SHOES);

        SqlStatements.Recorded<String> again = SqlStatements.during(() -> reads.name(A, EN));
        assertThat(again.result()).isEqualTo(SHOES);
        assertThat(again.count()).as(again.toString()).isZero();

        transactions.executeWithoutResult(status -> shops.findById(A.getId()).orElseThrow().rename(BOOTS));

        SqlStatements.Recorded<String> afterWrite = SqlStatements.during(() -> reads.name(A, EN));
        assertThat(afterWrite.result()).as("the write's store reads the database again").isEqualTo(BOOTS);
        assertThat(afterWrite.count()).isPositive();
        SqlStatements.Recorded<String> other = SqlStatements.during(() -> reads.name(B, EN));
        assertThat(other.count()).as("the other store's entry stays warm").isZero();
    }

    @Test
    void aBulkWriteEvictsAfterItsCommitAndTheRegionReadsOnTheMeters() throws Exception {
        reads.name(A, EN);

        transactions.executeWithoutResult(status -> {
            afterCommit.evictStoreAfterCommit(A, List.of(ShopRegions.NAME));
            assertThat(registry.region(ShopRegions.NAME, String.class).getIfPresent(CacheKey.of(A, EN)))
                    .as("not before the commit").isPresent();
        });

        assertThat(SqlStatements.during(() -> reads.name(A, EN)).count()).isPositive();
        assertThat(meters.get("cache.gets").tag(CACHE, ShopRegions.Names.NAME).tag("result", "miss").functionCounter()
                .count()).isPositive();
        assertThat(meters.get("cache.size").tag(CACHE, ShopRegions.Names.NAME).gauge().value()).isPositive();
        assertThat(registry.names()).containsExactly(ShopRegions.Names.NAME, ShopRegions.Names.SEARCH);
    }

    @Test
    void aBulkReadKeyedByTypedIdsLoadsOnceAndARegionSwitchedOffAlwaysMisses() {
        RegionCache<String> names = registry.region(ShopRegions.NAME, String.class);
        CacheKey sku = CacheKey.sku(A, EN, Sku.of("SKU-1"));
        CacheKey product = CacheKey.product(A, EN, ProductId.of(7)).with(PageableKeys.of(PageRequest.of(0, 20)));
        CacheKey variant = CacheKey.variant(A, EN, VariantId.of(9));
        CacheKey brand = CacheKey.of(A, EN).with(CategoryId.of(3), ManufacturerId.of(5));
        CacheKey slug = CacheKey.slug(A, EN, "shoes");
        CacheKey query = CacheKey.query(A, EN, QueryHash.of("q=shoes")).with(PageableKeys.of(PageRequest.of(1, 5)));
        CacheKey stock = CacheKey.sku(B, Sku.of("SKU-2"));
        List<CacheKey> keys = List.of(sku, product, variant, brand, slug, query, stock, sku);
        List<Set<CacheKey>> asked = new ArrayList<>();

        Map<CacheKey, String> first = names.getAll(keys, missing -> {
            asked.add(missing);
            Map<CacheKey, String> loaded = new HashMap<>();
            missing.stream().filter(key -> key != stock).forEach(key -> loaded.put(key, key.render()));
            return loaded;
        });
        Map<CacheKey, String> second = names.getAll(keys, missing -> {
            asked.add(missing);
            return Map.of();
        });

        assertThat(first).hasSize(6).containsEntry(slug, slug.render()).doesNotContainKey(stock);
        assertThat(second).isEqualTo(first);
        assertThat(asked).hasSize(2);
        assertThat(asked.get(0)).hasSize(7);
        assertThat(asked.get(1)).as("only the sku inventory never knew is asked again").containsExactly(stock);
        assertThat(product.render()).endsWith("|en|7|p0s20");
        assertThat(CacheKey.global(EN).isGlobal()).isTrue();
        names.evictAll(List.of(sku, variant));
        assertThat(names.getIfPresent(sku)).isEmpty();
        assertThat(names.getIfPresent(slug)).isPresent();
        names.evictStore(B);
        assertThat(names.stats().hits()).isPositive();

        Cache spring = cacheManager.getCache(ShopRegions.Names.NAME);
        assertThat(spring.get(slug, String.class)).isEqualTo(slug.render());
        assertThat(spring.get(brand, () -> "loaded")).isEqualTo(brand.render());
        spring.evict(slug);
        assertThat(spring.get(slug)).isNull();
        spring.put(slug, SHOES);
        assertThat(spring.get(slug).get()).isEqualTo(SHOES);
        spring.clear();
        assertThat(spring.get(brand)).isNull();

        RegionCache<String> search = registry.region(ShopRegions.SEARCH, String.class);
        search.put(slug, SHOES);
        assertThat(search.getIfPresent(slug)).as("switched off in configuration").isEmpty();
        assertThat(search.get(slug, key -> SHOES)).isEqualTo(SHOES);
        assertThat(search.getAll(List.of(slug), missing -> Map.of(slug, SHOES))).containsEntry(slug, SHOES);
        search.evict(slug);
        search.evictAll(List.of(slug));
        search.evictStore(A);
        search.clear();
        assertThat(search.stats().misses()).isEqualTo(3);
        assertThat(cacheManager.getCacheNames()).containsExactly(ShopRegions.Names.NAME, ShopRegions.Names.SEARCH);
    }
}
