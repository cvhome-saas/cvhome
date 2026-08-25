package com.asrevo.cvhome.catalog.utils;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.s2s.model.CdnProperties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The urls every product image on the storefront is fetched from. The base path is the CDN's rather than the
 * store's: one bucket serves every tenant and the store id is a folder inside it.
 */
class CloudFilePathUtilsTest {

    private static final String BASE = "https://cdn.example/bucket";

    private static final String STORE_ID = "65f023632bc46470c104b76f";

    private static final StoreMerchantId STORE = new StoreMerchantId(STORE_ID);

    private static final String CONTEXT = "/ctx";

    @Test
    void aStaticImagePathHangsUnderTheStoresFolder() {
        CloudFilePathUtils utils = new CloudFilePathUtils(new CdnProperties(BASE, CONTEXT));

        assertThat(utils.getBasePath(STORE)).isEqualTo(BASE);
        assertThat(utils.getContextPath()).isEqualTo(CONTEXT);
        assertThat(utils.buildStaticImageUtils(STORE, "logo.png"))
                .isEqualTo(String.format("%s/files/%s/logo.png", BASE, STORE_ID));
    }

    @Test
    void aBlankImageNameYieldsTheFolderItself() {
        CloudFilePathUtils utils = new CloudFilePathUtils(new CdnProperties(BASE, null));

        assertThat(utils.buildStaticImageUtils(STORE, "  "))
                .isEqualTo(String.format("%s/files/%s/", BASE, STORE_ID));
        // an unset context path is an empty prefix, never the string "null" in the middle of a url
        assertThat(utils.getContextPath()).isEmpty();
    }

    @Test
    void aProductImagePathIsKeyedBySku() {
        CloudFilePathUtils utils = new CloudFilePathUtils(new CdnProperties(BASE, ""));

        assertThat(utils.buildProductImageUtils(STORE, "SKU-1", "shoe.png"))
                .isEqualTo(String.format("%s/products/%s/SKU-1/SMALL/shoe.png", BASE, STORE_ID));
    }

}
