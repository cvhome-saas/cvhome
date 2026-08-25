package com.asrevo.cvhome.content.support;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.content.model.banner.BannerMeta;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The JSON columns are read back by builds newer than the one that wrote them, so unknown properties must not
 * fail the read.
 */
class JsonCodecTest {

    @Test
    void nullAndBlankReadBackAsNull() {
        assertThat(JsonCodec.write(null)).isNull();
        assertThat(JsonCodec.read(null, BannerMeta.class)).isNull();
        assertThat(JsonCodec.read("   ", BannerMeta.class)).isNull();
    }

    @Test
    void unknownPropertiesAreIgnored() {
        BannerMeta meta = JsonCodec.read("{\"loggedInOnly\":true,\"somethingNewer\":42}", BannerMeta.class);

        assertThat(meta).isNotNull();
        assertThat(meta.loggedInOnly()).isTrue();
    }

    @Test
    void roundTripKeepsTheDocument() {
        BannerMeta meta = new BannerMeta(null, null, null, true);

        assertThat(JsonCodec.read(JsonCodec.write(meta), BannerMeta.class)).isEqualTo(meta);
    }

}
