package com.asrevo.cvhome.cache.spring;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;

import com.asrevo.cvhome.cache.CacheRegistry;

/**
 * Stops the service at start-up when a {@code @Cacheable}, {@code @CachePut} or {@code @CacheEvict} names a region
 * the service does not declare: a typo would otherwise be a read that is never cached, found on a dashboard weeks
 * later, if at all.
 */
public final class CacheableRegionsValidator implements SmartInitializingSingleton {

    private final ListableBeanFactory beans;

    private final CacheRegistry registry;

    public CacheableRegionsValidator(ListableBeanFactory beans, CacheRegistry registry) {
        this.beans = beans;
        this.registry = registry;
    }

    @Override
    public void afterSingletonsInstantiated() {
        List<String> wrong = new ArrayList<>();
        for (String beanName : beans.getBeanNamesForType(Object.class, false, false)) {
            Class<?> type = beans.getType(beanName, false);
            if (type == null) {
                continue;
            }
            Class<?> target = AopUtils.isAopProxy(type) ? AopUtils.getTargetClass(type) : type;
            Map<Method, List<String>> named = MethodIntrospector.selectMethods(target,
                    (MethodIntrospector.MetadataLookup<List<String>>) CacheableRegionsValidator::cacheNamesOf);
            named.forEach((method, names) -> names.stream().filter(name -> registry.region(name).isEmpty())
                    .forEach(name -> wrong.add(String.format("%s.%s -> %s", target.getSimpleName(), method.getName(),
                            name))));
        }
        if (!wrong.isEmpty()) {
            throw new IllegalStateException(String.format("cached reads name regions this service does not declare (%s): %s",
                    registry.names(), wrong));
        }
    }

    private static List<String> cacheNamesOf(Method method) {
        List<String> names = new ArrayList<>();
        Cacheable cacheable = AnnotatedElementUtils.findMergedAnnotation(method, Cacheable.class);
        if (cacheable != null) {
            names.addAll(List.of(cacheable.cacheNames()));
        }
        CachePut put = AnnotatedElementUtils.findMergedAnnotation(method, CachePut.class);
        if (put != null) {
            names.addAll(List.of(put.cacheNames()));
        }
        CacheEvict evict = AnnotatedElementUtils.findMergedAnnotation(method, CacheEvict.class);
        if (evict != null) {
            names.addAll(List.of(evict.cacheNames()));
        }
        return names.isEmpty() ? null : names;
    }
}
