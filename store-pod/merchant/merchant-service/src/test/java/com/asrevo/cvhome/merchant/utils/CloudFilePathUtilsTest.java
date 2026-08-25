package com.asrevo.cvhome.merchant.utils;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.s2s.model.CdnProperties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Merchant media lives under {@code <cdn>/files/<store>/...} with no {@code IMAGE} segment — the layout the
 * S3 assets manager writes, so the URL must mirror it exactly or every logo 404s.
 */
class CloudFilePathUtilsTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");

    private static final String BASE = "http://cdn.test/bucket";

    private static final String CONTEXT = "/ctx";

    @Test
    void basePathIsTheCdnWhateverTheStore() {
        CloudFilePathUtils paths = new CloudFilePathUtils(new CdnProperties(BASE, CONTEXT));

        assertThat(paths.getBasePath(STORE)).isEqualTo(BASE);
        assertThat(paths.getContextPath()).isEqualTo(CONTEXT);
    }

    @Test
    void missingContextPathIsEmptyNotNull() {
        assertThat(new CloudFilePathUtils(new CdnProperties(BASE, null)).getContextPath()).isEmpty();
    }

    @Test
    void staticImagesSitDirectlyUnderTheStoreFolder() {
        CloudFilePathUtils paths = new CloudFilePathUtils(new CdnProperties(BASE, null));

        assertThat(paths.buildStaticImageUtils(STORE, "a.png"))
                .isEqualTo(String.format("%s/files/%s/a.png", BASE, STORE.getId()));
        assertThat(paths.buildStaticImageUtils(STORE, " "))
                .isEqualTo(String.format("%s/files/%s/", BASE, STORE.getId()));
    }

}
