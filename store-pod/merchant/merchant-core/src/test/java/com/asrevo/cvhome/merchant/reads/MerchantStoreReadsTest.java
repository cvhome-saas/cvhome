package com.asrevo.cvhome.merchant.reads;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.annotation.AnnotatedElementUtils;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.entity.merchant.MerchantStore;
import com.asrevo.cvhome.merchant.model.merchant.ReadableMerchantStore;
import com.asrevo.cvhome.merchant.service.facade.merchant.StoreFacade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MerchantStoreReadsTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b75f");

    private static final LanguageCode EN = LanguageCode.defaultLanguage();

    @Mock
    private StoreFacade stores;

    @InjectMocks
    private MerchantStoreReads reads;

    @Test
    void eachReadDelegatesAndIsDeclaredOnAMerchantRegion() throws Exception {
        ReadableMerchantStore store = new ReadableMerchantStore();
        when(stores.getReadableMerchantStoreId(STORE)).thenReturn(store);
        when(stores.getByMerchantStoreId(STORE, EN)).thenReturn(store);
        when(stores.supportedLanguages(STORE)).thenReturn(List.of(EN));

        assertThat(reads.store(STORE)).isSameAs(store);
        assertThat(reads.store(STORE, EN)).isSameAs(store);
        assertThat(reads.languages(STORE)).containsExactly(EN);

        List<String> names = List.of(MerchantRegions.values()).stream().map(MerchantRegions::regionName).toList();
        for (Method method : MerchantStoreReads.class.getDeclaredMethods()) {
            if (!Modifier.isPublic(method.getModifiers()) || method.isSynthetic()) {
                continue;
            }
            Cacheable cacheable = AnnotatedElementUtils.findMergedAnnotation(method, Cacheable.class);
            assertThat(cacheable).as(method.getName()).isNotNull();
            assertThat(names).contains(cacheable.cacheNames());
        }
        assertThat(names).containsExactly("merchant.store", "merchant.store-by-language", "merchant.languages");
        assertThat(MerchantRegions.STORE.ttl().toMinutes()).isEqualTo(5);
        assertThat(MerchantRegions.LANGUAGES.valueType()).isEqualTo(List.class);

        MerchantStore entity = new MerchantStore();
        assertThat(entity.scopedStore()).isNull();
        entity.setId(STORE);
        assertThat(entity.scopedStore()).isEqualTo(STORE);
    }
}
