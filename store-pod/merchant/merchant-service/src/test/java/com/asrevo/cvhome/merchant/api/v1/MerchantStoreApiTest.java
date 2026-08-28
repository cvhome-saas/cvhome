package com.asrevo.cvhome.merchant.api.v1;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.errors.MerchantStoreContextMismatchException;
import com.asrevo.cvhome.merchant.model.merchant.PersistableMerchantStore;
import com.asrevo.cvhome.merchant.model.merchant.ReadableMerchantStore;
import com.asrevo.cvhome.merchant.service.facade.merchant.StoreFacade;
import com.asrevo.cvhome.store.core.entity.content.InputContentFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * The controller's own logic: the path/tenant consistency check on the compatibility read, and how a multipart
 * upload becomes an {@link InputContentFile} — including the rename every slider image gets.
 */
class MerchantStoreApiTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");

    private static final LanguageCode LANGUAGE = new LanguageCode("en");

    private static final String FILE = "file";

    private static final String LOGO = "logo.png";

    private static final String PNG = "image/png";

    private static final byte[] BYTES = {1, 2, 3};

    private static final String SLIDER_URL = "http://cdn/slider";

    private static final String SLIDE = "slide.jpeg";

    private final StoreFacade storeFacade = mock(StoreFacade.class);

    private final MerchantStoreApi api = new MerchantStoreApi(storeFacade);

    @Test
    void compatibilityReadRejectsDifferentTenantContext() {
        assertThatThrownBy(() -> api.store("65f023632bc46470c104b75f", STORE, LANGUAGE))
                .isInstanceOf(MerchantStoreContextMismatchException.class);

        verifyNoInteractions(storeFacade);
    }

    @Test
    void compatibilityReadUsesResolvedTenantContext() throws Exception {
        ReadableMerchantStore expected = new ReadableMerchantStore();
        when(storeFacade.getByMerchantStoreId(STORE, LANGUAGE)).thenReturn(expected);

        assertThat(api.store(STORE.getId(), STORE, LANGUAGE)).isSameAs(expected);
        assertThat(api.storeFull(STORE, LANGUAGE)).isSameAs(expected);
    }

    @Test
    void languagesComeFromTheFacade() {
        when(storeFacade.supportedLanguages(STORE)).thenReturn(List.of(LANGUAGE));

        assertThat(api.supportedLanguages(STORE)).containsExactly(LANGUAGE);
    }

    @Test
    void writesDelegateToTheFacade() throws Exception {
        PersistableMerchantStore store = new PersistableMerchantStore();

        api.create(store);
        api.update(STORE, store);
        api.delete(STORE);

        verify(storeFacade).create(store);
        verify(storeFacade).update(STORE, store);
        verify(storeFacade).delete(STORE);
    }

}
