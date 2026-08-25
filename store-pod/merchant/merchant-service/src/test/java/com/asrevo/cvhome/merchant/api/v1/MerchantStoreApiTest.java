package com.asrevo.cvhome.merchant.api.v1;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.ReadableSliderImage;
import com.asrevo.cvhome.commons.domain.SliderImage;
import com.asrevo.cvhome.commons.domain.SocialLink;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.errors.MerchantStoreContextMismatchException;
import com.asrevo.cvhome.merchant.errors.UploadedFileUnreadableException;
import com.asrevo.cvhome.merchant.model.merchant.PersistableMerchantStore;
import com.asrevo.cvhome.merchant.model.merchant.ReadableMerchantStore;
import com.asrevo.cvhome.merchant.service.facade.merchant.StoreFacade;
import com.asrevo.cvhome.store.core.entity.content.FileContentType;
import com.asrevo.cvhome.store.core.entity.content.InputContentFile;
import com.asrevo.cvhome.store.utils.ImageFilePath;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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

    private final ImageFilePath imageFilePath = mock(ImageFilePath.class);

    private final MerchantStoreApi api = new MerchantStoreApi(storeFacade, imageFilePath);

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
        Set<SocialLink> links = Set.of(new SocialLink("X", "https://x.com/shop"));
        List<SliderImage> slides = List.of(new SliderImage(0, "a.png"));
        store.setSocialLinks(links);
        store.setSliderImages(slides);

        api.create(store);
        api.update(STORE, store);
        api.updateSocialLinks(STORE, store);
        api.sliderImages(STORE, store);
        api.delete(STORE);

        verify(storeFacade).create(store);
        verify(storeFacade).update(STORE, store);
        verify(storeFacade).updateSocialLinks(STORE, links);
        verify(storeFacade).updateSliderImages(STORE, slides);
        verify(storeFacade).delete(STORE);
    }

    @Test
    void logoUploadCarriesNameTypeAndBytes() throws Exception {
        api.addLogo(STORE, new MockMultipartFile(FILE, LOGO, PNG, BYTES));

        ArgumentCaptor<InputContentFile> sent = ArgumentCaptor.forClass(InputContentFile.class);
        verify(storeFacade).addStoreLogo(eq(STORE), sent.capture());
        InputContentFile file = sent.getValue();
        assertThat(file.getFileName()).isEqualTo(LOGO);
        assertThat(file.getMimeType()).isEqualTo(PNG);
        assertThat(file.getFileContentType()).isEqualTo(FileContentType.LOGO);
        assertThat(file.getFile().readAllBytes()).isEqualTo(BYTES);
    }

    @Test
    void bannerUploadIsTaggedAsBanner() throws Exception {
        api.addBanner(STORE, new MockMultipartFile(FILE, "banner.png", PNG, BYTES));

        ArgumentCaptor<InputContentFile> sent = ArgumentCaptor.forClass(InputContentFile.class);
        verify(storeFacade).addStoreBanner(eq(STORE), sent.capture());
        assertThat(sent.getValue().getFileContentType()).isEqualTo(FileContentType.BANNER);
    }

    @Test
    void sliderImageIsRenamedToAUniqueNameKeepingItsExtension() throws Exception {
        when(storeFacade.addStoreSliderImage(eq(STORE), any())).thenAnswer(
                invocation -> new SliderImage(3, invocation.<InputContentFile>getArgument(1).getFileName()));
        when(imageFilePath.buildStoreSliderFilePath(eq(STORE), anyString())).thenReturn(SLIDER_URL);

        ReadableSliderImage image = api.addSliderImage(STORE, new MockMultipartFile(FILE, SLIDE, PNG, BYTES));

        assertThat(image.priority()).isEqualTo(3);
        assertThat(image.name()).endsWith(".jpeg").isNotEqualTo(SLIDE);
        assertThat(image.url()).isEqualTo(SLIDER_URL);
        verify(imageFilePath).buildStoreSliderFilePath(STORE, image.name());
    }

    @Test
    void unreadableUploadIsReportedAsOurFailureNotTheCallers() throws IOException {
        MultipartFile broken = mock(MultipartFile.class);
        when(broken.getOriginalFilename()).thenReturn(LOGO);
        when(broken.getBytes()).thenThrow(new IOException("connection reset"));

        assertThatThrownBy(() -> api.addLogo(STORE, broken)).isInstanceOf(UploadedFileUnreadableException.class);

        verifyNoInteractions(storeFacade);
    }

}
