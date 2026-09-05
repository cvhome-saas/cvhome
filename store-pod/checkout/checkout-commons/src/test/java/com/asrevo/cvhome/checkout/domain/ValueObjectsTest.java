package com.asrevo.cvhome.checkout.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValueObjectsTest {

    @Test
    void refsAreUuidsThatPrintAsTheirValue() {
        OrderRef ref = OrderRef.newRef();
        assertThat(ref.value()).hasSize(36);
        assertThat(OrderRef.of(ref.value())).isEqualTo(ref);
        assertThat(ref.toString()).isEqualTo(ref.value());
        assertThatThrownBy(() -> OrderRef.of(" ")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new OrderRef(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void cartCodesBehaveTheSame() {
        CartCode code = CartCode.newCode();
        assertThat(CartCode.of(code.value())).isEqualTo(code);
        assertThat(code.toString()).isEqualTo(code.value());
        assertThatThrownBy(() -> CartCode.of("")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aShopperNeedsASubject() {
        String sub = "abc";
        assertThat(new ShopperId(sub).sub()).isEqualTo(sub);
        assertThatThrownBy(() -> new ShopperId("")).isInstanceOf(IllegalArgumentException.class);
    }
}
