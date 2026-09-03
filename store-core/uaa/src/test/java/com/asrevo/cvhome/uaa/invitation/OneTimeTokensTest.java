package com.asrevo.cvhome.uaa.invitation;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OneTimeTokensTest {

    @Test
    void tokensAreUrlSafeAndNeverRepeat() {
        String a = OneTimeTokens.newToken();
        String b = OneTimeTokens.newToken();

        assertThat(a).isNotEqualTo(b).matches("[A-Za-z0-9_-]{43}");
    }

    @Test
    void theHashIsStableAndNotTheToken() {
        String token = OneTimeTokens.newToken();

        assertThat(OneTimeTokens.hash(token)).isEqualTo(OneTimeTokens.hash(token)).isNotEqualTo(token)
                .isNotEqualTo(OneTimeTokens.hash(OneTimeTokens.newToken()));
    }

}
