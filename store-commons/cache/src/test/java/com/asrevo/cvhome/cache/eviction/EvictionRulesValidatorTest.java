package com.asrevo.cvhome.cache.eviction;

import java.util.Set;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.cache.TestRegions;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.commons.domain.StoreScoped;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EvictionRulesValidatorTest {

    private static final String PACKAGE = "com.asrevo.cvhome.cache.eviction";

    record Scoped(StoreMerchantId store) implements StoreScoped {

        @Override
        public StoreMerchantId scopedStore() {
            return store;
        }
    }

    static final class Unscoped {
    }

    static final class Unruled {
    }

    private static EntityManagerFactory mapping(Class<?>... types) {
        Metamodel metamodel = mock(Metamodel.class);
        Set<EntityType<?>> entities = new java.util.LinkedHashSet<>();
        for (Class<?> type : types) {
            EntityType<?> entity = mock(EntityType.class);
            when(entity.getJavaType()).thenAnswer(invocation -> type);
            entities.add(entity);
        }
        EntityType<?> nameless = mock(EntityType.class);
        entities.add(nameless);
        when(metamodel.getEntities()).thenReturn(entities);
        EntityManagerFactory factory = mock(EntityManagerFactory.class);
        when(factory.getMetamodel()).thenReturn(metamodel);
        return factory;
    }

    @Test
    void aRuledEntityMustNameItsStoreAndAnUnruledOneIsOnlyLogged() {
        EvictionRules scoped = EvictionRules.in(PACKAGE).on(Scoped.class).evict(TestRegions.PRODUCT).build();
        EvictionRules unscoped = EvictionRules.in(PACKAGE).on(Unscoped.class).evict(TestRegions.PRODUCT).build();

        assertThatCode(() -> new EvictionRulesValidator(scoped, mapping(Scoped.class, Unruled.class, String.class))
                .afterSingletonsInstantiated()).doesNotThrowAnyException();
        assertThatThrownBy(() -> new EvictionRulesValidator(unscoped, mapping(Unscoped.class))
                .afterSingletonsInstantiated()).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Unscoped");
        assertThatCode(() -> new EvictionRulesValidator(EvictionRules.none(), mapping(Unscoped.class))
                .afterSingletonsInstantiated()).doesNotThrowAnyException();
        assertThatCode(() -> new EvictionRulesValidator(unscoped, null).afterSingletonsInstantiated())
                .doesNotThrowAnyException();
    }
}
