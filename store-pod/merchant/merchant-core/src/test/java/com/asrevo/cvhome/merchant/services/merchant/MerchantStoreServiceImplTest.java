package com.asrevo.cvhome.merchant.services.merchant;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.commons.domain.SliderImage;
import com.asrevo.cvhome.commons.domain.SocialLink;
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

    @Test
    void socialLinksAreReplacedOnTheStoredRow() {
        MerchantStore store = new MerchantStore(STORE, NAME);
        when(repository.findByMerchantStoreId(STORE)).thenReturn(store);
        Set<SocialLink> links = Set.of(new SocialLink("INSTAGRAM", "https://instagram.com/shop"));

        service.updateSocialLinks(STORE, links);

        assertThat(store.getSocialLinks()).isEqualTo(links);
        verify(repository).saveAndFlush(store);
    }

    @Test
    void sliderImagesAreReplacedOnTheStoredRow() {
        MerchantStore store = new MerchantStore(STORE, NAME);
        when(repository.findByMerchantStoreId(STORE)).thenReturn(store);
        List<SliderImage> slides = List.of(new SliderImage(0, "a.png"), new SliderImage(1, "b.png"));

        service.updateSliderImages(STORE, slides);

        assertThat(store.getSliderImages()).isEqualTo(slides);
        verify(repository).saveAndFlush(store);
    }

}
