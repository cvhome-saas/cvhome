package com.asrevo.cvhome.cache.config;

import java.util.List;

import jakarta.persistence.EntityManagerFactory;

import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.asrevo.cvhome.cache.CacheRegions;
import com.asrevo.cvhome.cache.CacheRegistry;
import com.asrevo.cvhome.cache.eviction.AfterCommitEviction;
import com.asrevo.cvhome.cache.eviction.CacheEvictionIntegrator;
import com.asrevo.cvhome.cache.eviction.CommitEvictionListener;
import com.asrevo.cvhome.cache.eviction.EvictionRules;
import com.asrevo.cvhome.cache.eviction.EvictionRulesValidator;
import com.asrevo.cvhome.cache.metrics.RegionCacheMeterBinderProvider;
import com.asrevo.cvhome.cache.provider.CacheProvider;
import com.asrevo.cvhome.cache.provider.caffeine.CaffeineCacheProvider;
import com.asrevo.cvhome.cache.spring.CacheableRegionsValidator;
import com.asrevo.cvhome.cache.spring.CvhomeCacheManager;
import com.asrevo.cvhome.cache.spring.StoreScopedKeyGenerator;
import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * Caching for every service that has this module: the Caffeine provider, the registry built from every
 * {@link CacheRegions} bean (none declared means no region and nothing cached), Spring's cache abstraction over it
 * with the store-scoped key generator, and the start-up check of the names {@code @Cacheable} uses.
 *
 * <p>
 * Configured before Boot's own cache auto-configuration, which would otherwise build a Caffeine manager of its own
 * from {@code spring.cache.*} and leave every {@code @Cacheable} of the service pointing at it.
 * Every bean here always exists; the switches (a region's provider, time-to-live, size, on or off) are read from
 * {@link CacheProperties} inside the registry, never as a bean condition, because a native image fixes the bean
 * graph when it is built. A remote provider is a bean of {@link CacheProvider} in its own module and needs nothing
 * here.
 * </p>
 */
@AutoConfiguration
@AutoConfigureBefore(name = "org.springframework.boot.cache.autoconfigure.CacheAutoConfiguration")
@EnableCaching
@ConditionalOnClass(Caffeine.class)
@EnableConfigurationProperties(CacheProperties.class)
public class CacheAutoConfiguration {

    @Bean
    CaffeineCacheProvider caffeineCacheProvider() {
        return new CaffeineCacheProvider();
    }

    /**
     * Every {@link CacheRegions} bean in the context, merged: the service's own enum and the ones a library it uses
     * declares for its own reads (the merchant client's). None at all means nothing is cached.
     */
    @Bean
    CacheRegistry cacheRegistry(ObjectProvider<CacheRegions> declarations, ObjectProvider<CacheProvider> providers,
                                CacheProperties properties) {
        List<CacheProvider> present = providers.orderedStream().toList();
        return new CacheRegistry(CacheRegions.merge(declarations.orderedStream().toList()), present, properties);
    }

    @Bean
    @ConditionalOnMissingBean(CacheManager.class)
    CvhomeCacheManager cacheManager(CacheRegistry registry) {
        return new CvhomeCacheManager(registry);
    }

    @Bean(StoreScopedKeyGenerator.BEAN)
    StoreScopedKeyGenerator storeScopedKeys(CacheRegistry registry) {
        return new StoreScopedKeyGenerator(registry);
    }

    @Bean
    CacheableRegionsValidator cacheableRegionsValidator(ListableBeanFactory beans, CacheRegistry registry) {
        return new CacheableRegionsValidator(beans, registry);
    }

    /** Boot binds every region's meters at start-up through this, as it does a Caffeine cache's. */
    @Bean
    @ConditionalOnClass(name = "org.springframework.boot.cache.metrics.CacheMeterBinderProvider")
    RegionCacheMeterBinderProvider regionCacheMeterBinderProvider() {
        return new RegionCacheMeterBinderProvider();
    }

    /**
     * Eviction on commit, for the services that have Hibernate: the listener the service's {@link EvictionRules}
     * drive (none declared means no eviction), installed through Hibernate's integrator hook, and the check of the
     * rules against the mapped entities once the context is up.
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = {"org.hibernate.integrator.spi.Integrator",
        "org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer"})
    static class Eviction {

        @Bean
        @ConditionalOnMissingBean(EvictionRules.class)
        EvictionRules evictionRules() {
            return EvictionRules.none();
        }

        @Bean
        CommitEvictionListener commitEvictionListener(EvictionRules rules, CacheRegistry registry) {
            return new CommitEvictionListener(rules, registry);
        }

        @Bean
        CacheEvictionIntegrator cacheEvictionIntegrator(CommitEvictionListener listener) {
            return new CacheEvictionIntegrator(listener);
        }

        @Bean
        HibernatePropertiesCustomizer cacheEvictionIntegratorProperty(CacheEvictionIntegrator integrator) {
            return properties -> properties.putAll(integrator.asProperty());
        }

        @Bean
        @ConditionalOnClass(name = "org.springframework.transaction.support.TransactionSynchronizationManager")
        AfterCommitEviction afterCommitEviction(CacheRegistry registry) {
            return new AfterCommitEviction(registry);
        }

        @Bean
        EvictionRulesValidator evictionRulesValidator(EvictionRules rules, ObjectProvider<EntityManagerFactory> factory) {
            EntityManagerFactory present = factory.getIfAvailable();
            return new EvictionRulesValidator(rules, present);
        }
    }
}
