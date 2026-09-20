package com.asrevo.cvhome.cache.spring;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;

import com.asrevo.cvhome.cache.CacheRegions;
import com.asrevo.cvhome.cache.CacheRegistry;
import com.asrevo.cvhome.cache.TestRegions;
import com.asrevo.cvhome.cache.config.CacheProperties;
import com.asrevo.cvhome.cache.provider.caffeine.CaffeineCacheProvider;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CacheableRegionsValidatorTest {

    static class Declared {

        @Cacheable("test.product")
        String product() {
            return "";
        }

        @CachePut("test.listing")
        String refresh() {
            return "";
        }

        @CacheEvict("test.country")
        void drop() {
            // nothing
        }

        String plain() {
            return "";
        }
    }

    static class Undeclared {

        @Cacheable("test.produkt")
        String product() {
            return "";
        }
    }

    private static CacheRegistry registry() {
        return new CacheRegistry(CacheRegions.of(TestRegions.values()), List.of(new CaffeineCacheProvider()),
                CacheProperties.defaults());
    }

    @Test
    void declaredNamesPassAndAnUndeclaredOneStopsTheStart() {
        DefaultListableBeanFactory beans = new DefaultListableBeanFactory();
        beans.registerBeanDefinition("declared", new RootBeanDefinition(Declared.class));
        assertThatCode(() -> new CacheableRegionsValidator(beans, registry()).afterSingletonsInstantiated())
                .doesNotThrowAnyException();

        beans.registerBeanDefinition("undeclared", new RootBeanDefinition(Undeclared.class));
        assertThatThrownBy(() -> new CacheableRegionsValidator(beans, registry()).afterSingletonsInstantiated())
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("Undeclared.product -> test.produkt");
    }
}
