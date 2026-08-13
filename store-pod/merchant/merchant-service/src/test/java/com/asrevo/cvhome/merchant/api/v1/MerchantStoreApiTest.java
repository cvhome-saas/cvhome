package com.asrevo.cvhome.merchant.api.v1;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.errors.MerchantStoreContextMismatchException;
import com.asrevo.cvhome.merchant.model.merchant.ReadableMerchantStore;
import com.asrevo.cvhome.merchant.service.facade.merchant.StoreFacade;
import com.asrevo.cvhome.store.utils.ImageFilePath;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class MerchantStoreApiTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("store-one");

    private static final LanguageCode LANGUAGE = new LanguageCode("en");

    private final StoreFacade storeFacade = mock(StoreFacade.class);

    private final MerchantStoreApi api = new MerchantStoreApi(storeFacade, mock(ImageFilePath.class));

    @Test
    void compatibilityReadRejectsDifferentTenantContext() {
        assertThrows(MerchantStoreContextMismatchException.class,
                () -> api.store("store-two", STORE, LANGUAGE));

        verifyNoInteractions(storeFacade);
    }

    @Test
    void compatibilityReadUsesResolvedTenantContext() throws Exception {
        ReadableMerchantStore expected = new ReadableMerchantStore();
        when(storeFacade.getByMerchantStoreId(STORE, LANGUAGE)).thenReturn(expected);

        ReadableMerchantStore actual = api.store(STORE.getId().toString(), STORE, LANGUAGE);

        assertSame(expected, actual);
    }

}
