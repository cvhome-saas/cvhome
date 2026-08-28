package com.asrevo.cvhome.merchant.services.merchant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.entity.merchant.MerchantStore;
import com.asrevo.cvhome.merchant.repositories.merchant.MerchantRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MerchantStoreServiceImplTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");

    private static final String NAME = "Shop";

    private MerchantRepository repository;

    private MerchantStoreServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(MerchantRepository.class);
        service = new MerchantStoreServiceImpl(repository);
    }

    @Test
    void lookupUsesTheLanguageFetchingQuery() {
        MerchantStore store = new MerchantStore(STORE, NAME);
        when(repository.findByMerchantStoreId(STORE)).thenReturn(store);

        assertThat(service.getByMerchantStoreId(STORE)).isSameAs(store);
    }

    @Test
    void saveOrUpdateFlushesImmediately() {
        MerchantStore store = new MerchantStore(STORE, NAME);

        service.saveOrUpdate(store);

        verify(repository).saveAndFlush(store);
    }

}
