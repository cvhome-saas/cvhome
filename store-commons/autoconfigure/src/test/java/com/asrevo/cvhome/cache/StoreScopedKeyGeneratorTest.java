package com.asrevo.cvhome.cache;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** A cached read is keyed by its store first, whatever position the store argument has, and refused without one. */
class StoreScopedKeyGeneratorTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("store-a");

    private static final String CODE = "code";

    private final StoreScopedKeyGenerator generator = new StoreScopedKeyGenerator();

    private static Method method() throws NoSuchMethodException {
        return StoreScopedKeyGeneratorTest.class.getDeclaredMethod("method");
    }

    @Test
    void theStoreComesFirstAndTheOtherArgumentsFollowInOrderNullsIncluded() throws Exception {
        Object key = generator.generate(this, method(), CODE, STORE, null, 8);

        assertThat(key).isEqualTo(new StoreScopedKey(STORE, Arrays.asList(CODE, null, 8)));
        assertThat(generator.generate(this, method(), CODE, STORE, null, 8)).hasSameHashCodeAs(key);
        assertThat(generator.generate(this, method(), CODE, new StoreMerchantId("store-b"), null, 8))
                .isNotEqualTo(key);
    }

    @Test
    void aReadWithoutAStoreIsRefused() throws Exception {
        Method method = method();
        assertThatThrownBy(() -> generator.generate(this, method, CODE, 8))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("StoreScopedKeyGeneratorTest.method");
    }
}
