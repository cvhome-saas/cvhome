package com.asrevo.cvhome.payment.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.s2s.model.CdnProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CloudFilePathUtilsTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("store-1");

    private static final String BASE = "https://cdn.example";

    private static final String CONTEXT = "/ctx";

    @Mock
    private CdnProperties cdn;

    @Test
    void basePathComesFromTheCdnRegardlessOfStore() {
        when(cdn.basePath()).thenReturn(BASE);

        assertThat(new CloudFilePathUtils(cdn).getBasePath(STORE)).isEqualTo(BASE);
    }

    @Test
    void contextPathIsNeverNull() {
        when(cdn.contextPath()).thenReturn(null).thenReturn(CONTEXT);
        CloudFilePathUtils utils = new CloudFilePathUtils(cdn);

        assertThat(utils.getContextPath()).isEmpty();
        assertThat(utils.getContextPath()).isEqualTo(CONTEXT);
    }

    @Test
    void staticImagePathIsScopedToTheStoreAndTheNameIsOptional() {
        when(cdn.basePath()).thenReturn(BASE);
        CloudFilePathUtils utils = new CloudFilePathUtils(cdn);

        assertThat(utils.buildStaticImageUtils(STORE, "logo.png")).isEqualTo("https://cdn.example/files/store-1/logo.png");
        assertThat(utils.buildStaticImageUtils(STORE, " ")).isEqualTo("https://cdn.example/files/store-1/");
    }

}
