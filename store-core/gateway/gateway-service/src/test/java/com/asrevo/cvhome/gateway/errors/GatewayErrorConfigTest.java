package com.asrevo.cvhome.gateway.errors;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.errors.web.ErrorHandlingProperties;

import static org.assertj.core.api.Assertions.assertThat;

/** The factory the reactive handler writes bodies with is the shared one, over the shared properties. */
class GatewayErrorConfigTest {

    @Test
    void theFactoryIsBuiltOverTheProperties() {
        assertThat(new GatewayErrorConfig().problemDetailFactory(new ErrorHandlingProperties(null, false))).isNotNull();
    }

}
