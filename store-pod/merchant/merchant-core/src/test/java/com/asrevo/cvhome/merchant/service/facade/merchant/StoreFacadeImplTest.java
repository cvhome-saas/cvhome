package com.asrevo.cvhome.merchant.service.facade.merchant;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.asrevo.cvhome.commons.domain.DomainType;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.ManagerStoreDomain;
import com.asrevo.cvhome.commons.domain.SliderImage;
import com.asrevo.cvhome.commons.domain.SocialLink;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.commons.utils.DefaultStoresConstants;
import com.asrevo.cvhome.merchant.entity.merchant.MerchantStore;
import com.asrevo.cvhome.merchant.errors.DefaultStoreNotRemovableException;
import com.asrevo.cvhome.merchant.errors.DuplicateMerchantStoreException;
import com.asrevo.cvhome.merchant.errors.MerchantStoreNotFoundException;
import com.asrevo.cvhome.merchant.model.merchant.PersistableMerchantStore;
import com.asrevo.cvhome.merchant.model.merchant.ReadableMerchantStore;
import com.asrevo.cvhome.merchant.service.populator.merchant.PersistableMerchantStorePopulator;
import com.asrevo.cvhome.merchant.service.populator.merchant.ReadableMerchantStorePopulator;
import com.asrevo.cvhome.merchant.services.merchant.MerchantStoreService;
import com.asrevo.cvhome.store.core.entity.content.FileContentType;
import com.asrevo.cvhome.store.core.entity.content.InputContentFile;
import com.asrevo.cvhome.store.core.modules.cms.content.ContentAssetsManager;
import com.asrevo.cvhome.store.core.modules.cms.errors.AssetUploadFailedException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * The store facade's rules: a taken id is a conflict, the default store is never deleted, every upload is tagged
 * with its content type and its stream is closed whatever happens, and a new slider image takes the next priority.
 */
class StoreFacadeImplTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b75f");

    private static final StoreMerchantId UNKNOWN = new StoreMerchantId("65f023632bc46470c104b00f");

    private static final LanguageCode EN = new LanguageCode("en");

    private static final LanguageCode FR = new LanguageCode("fr");

    private static final String NAME = "My Store";

    private static final String ORG = "21f023932bc66470c104b76f";

    private static final String LOGO = "logo.png";

    private static final String BANNER = "banner.png";

    private static final String SLIDE = "slide.png";

    private static final String FOLDER = "marketing";

    private MerchantStoreService service;

    private PersistableMerchantStorePopulator persistablePopulator;

    private ReadableMerchantStorePopulator readablePopulator;

    private ContentAssetsManager assets;

    private StoreFacadeImpl facade;

    @BeforeEach
    void setUp() {
        service = mock(MerchantStoreService.class);
        persistablePopulator = mock(PersistableMerchantStorePopulator.class);
        readablePopulator = mock(ReadableMerchantStorePopulator.class);
        assets = mock(ContentAssetsManager.class);
        facade = new StoreFacadeImpl(service, persistablePopulator, readablePopulator, assets);
    }

    private MerchantStore stored() {
        MerchantStore store = new MerchantStore(STORE, NAME);
        store.setOrg(ORG);
        store.setDefaultLanguageCode(FR);
        store.setLanguages(List.of(FR, EN));
        when(service.getByMerchantStoreId(STORE)).thenReturn(store);
        return store;
    }

    private static InputContentFile upload(String name, InputStream stream) {
        InputContentFile file = new InputContentFile();
        file.setFileName(name);
        file.setFile(stream);
        return file;
    }

    private static TrackedStream stream() {
        return new TrackedStream();
    }

    // ------------------------------------------------------------------------------------------------- reads

    @Test
    void getDelegatesToTheService() {
        MerchantStore store = stored();

        assertThat(facade.get(STORE)).isSameAs(store);
    }

    @Test
    void readByIdPopulatesInTheRequestedLanguage() throws MerchantStoreNotFoundException {
        MerchantStore store = stored();

        ReadableMerchantStore readable = facade.getByMerchantStoreId(STORE, EN);

        assertThat(readable).isNotNull();
        verify(readablePopulator).populate(eq(store), any(ReadableMerchantStore.class), eq(store), eq(EN));
    }

    @Test
    void readOfUnknownStoreIsNotFound() {
        assertThatThrownBy(() -> facade.getByMerchantStoreId(UNKNOWN, EN))
                .isInstanceOf(MerchantStoreNotFoundException.class);
    }

    @Test
    void readWithoutLanguageUsesTheStoreDefault() {
        MerchantStore store = stored();

        facade.getReadableMerchantStoreId(STORE);

        verify(readablePopulator).populate(eq(store), any(ReadableMerchantStore.class), eq(store), eq(FR));
    }

    @Test
    void supportedLanguagesComeFromTheStore() {
        stored();

        assertThat(facade.supportedLanguages(STORE)).containsExactly(FR, EN);
        assertThat(facade.supportedLanguages(UNKNOWN)).isEmpty();
    }

    // ----------------------------------------------------------------------------------------- create / update

    @Test
    void createRefusesATakenId() {
        stored();
        PersistableMerchantStore request = new PersistableMerchantStore();
        request.setId(STORE.getId());

        assertThatThrownBy(() -> facade.create(request)).isInstanceOf(DuplicateMerchantStoreException.class);

        verify(service, never()).saveOrUpdate(any());
    }

    @Test
    void createAllocatesTheNameAsSubDomainAndSaves() throws DuplicateMerchantStoreException {
        PersistableMerchantStore request = new PersistableMerchantStore();
        request.setId(UNKNOWN.getId());
        request.setName(NAME);
        MerchantStore populated = new MerchantStore(UNKNOWN, NAME);
        when(persistablePopulator.populate(eq(request), any(MerchantStore.class), eq(LanguageCode.defaultLanguage())))
                .thenReturn(populated);

        facade.create(request);

        assertThat(request.getStoreDomains()).containsExactly(new ManagerStoreDomain(NAME, DomainType.SUB_DOMAIN));
        verify(service).saveOrUpdate(populated);
    }

    @Test
    void updateCarriesIdentityAndMediaOverFromTheStoredRow() throws MerchantStoreNotFoundException {
        MerchantStore store = stored();
        Set<SocialLink> links = Set.of(new SocialLink("X", "https://x.com/me"));
        List<SliderImage> slides = List.of(new SliderImage(0, SLIDE));
        Set<ManagerStoreDomain> domains = Set.of(new ManagerStoreDomain("me", DomainType.SUB_DOMAIN));
        store.setSocialLinks(links);
        store.setSliderImages(slides);
        store.setStoreDomains(domains);
        PersistableMerchantStore request = new PersistableMerchantStore();
        request.setId("something-else");
        request.setOrg("another-org");
        when(persistablePopulator.populate(request, store, LanguageCode.defaultLanguage())).thenReturn(store);

        facade.update(STORE, request);

        assertThat(request.getId()).isEqualTo(STORE.getId());
        assertThat(request.getOrg()).isEqualTo(ORG);
        assertThat(request.getSocialLinks()).isEqualTo(links);
        assertThat(request.getSliderImages()).isEqualTo(slides);
        assertThat(request.getStoreDomains()).isEqualTo(domains);
        verify(service).update(store);
    }

    @Test
    void updateOfUnknownStoreIsNotFound() {
        assertThatThrownBy(() -> facade.update(UNKNOWN, new PersistableMerchantStore()))
                .isInstanceOf(MerchantStoreNotFoundException.class);
        verify(service, never()).update(any());
    }

    @Test
    void socialLinksAndSliderImagesGoStraightToTheService() {
        Set<SocialLink> links = Set.of(new SocialLink("TIKTOK", "https://tiktok.com/@me"));
        List<SliderImage> slides = List.of(new SliderImage(1, SLIDE));

        facade.updateSocialLinks(STORE, links);
        facade.updateSliderImages(STORE, slides);

        verify(service).updateSocialLinks(STORE, links);
        verify(service).updateSliderImages(STORE, slides);
    }

    // ------------------------------------------------------------------------------------------------ delete

    @Test
    void theDefaultStoreCannotBeDeleted() {
        assertThatThrownBy(() -> facade.delete(DefaultStoresConstants.DEFAULT_ORG1_STORE1))
                .isInstanceOf(DefaultStoreNotRemovableException.class);
        verifyNoInteractions(service);
    }

    @Test
    void deleteOfUnknownStoreIsNotFound() {
        assertThatThrownBy(() -> facade.delete(UNKNOWN)).isInstanceOf(MerchantStoreNotFoundException.class);
        verify(service, never()).delete(any());
    }

    @Test
    void deleteRemovesTheStoredRow() throws MerchantStoreNotFoundException, DefaultStoreNotRemovableException {
        MerchantStore store = stored();

        facade.delete(STORE);

        verify(service).delete(store);
    }

    // ----------------------------------------------------------------------------------------------- uploads

    @Test
    void logoIsUploadedAsLogoAndRecordedOnTheStore() throws AssetUploadFailedException {
        MerchantStore store = stored();
        TrackedStream stream = stream();
        InputContentFile file = upload(LOGO, stream);

        facade.addStoreLogo(STORE, file);

        assertThat(file.getFileContentType()).isEqualTo(FileContentType.LOGO);
        assertThat(store.getStoreLogo()).isEqualTo(LOGO);
        assertThat(stream.closed).isTrue();
        verify(assets).addFile(STORE.getId(), Optional.empty(), file);
        verify(service).save(store);
    }

    @Test
    void bannerIsUploadedAsBannerAndRecordedOnTheStore() throws AssetUploadFailedException {
        MerchantStore store = stored();
        InputContentFile file = upload(BANNER, stream());
        file.setPath(FOLDER);

        facade.addStoreBanner(STORE, file);

        assertThat(file.getFileContentType()).isEqualTo(FileContentType.BANNER);
        assertThat(store.getStoreBanner()).isEqualTo(BANNER);
        verify(assets).addFile(STORE.getId(), Optional.of(FOLDER), file);
        verify(service).save(store);
    }

    @Test
    void sliderImageTakesTheNextPriority() throws AssetUploadFailedException {
        MerchantStore store = stored();
        store.setSliderImages(List.of(new SliderImage(0, "a.png"), new SliderImage(4, "b.png")));
        InputContentFile file = upload(SLIDE, stream());

        SliderImage added = facade.addStoreSliderImage(STORE, file);

        assertThat(added).isEqualTo(new SliderImage(5, SLIDE));
        assertThat(file.getFileContentType()).isEqualTo(FileContentType.SLIDER);
        assertThat(store.getSliderImages()).hasSize(3).contains(added);
        verify(assets).addFile(eq(STORE.getId()), eq(Optional.empty()), eq(file));
        verify(service).save(store);
    }

    @Test
    void firstSliderImageStartsAtZero() {
        stored();

        SliderImage added = facade.addStoreSliderImage(STORE, upload(SLIDE, stream()));

        assertThat(added.priority()).isZero();
    }

    @Test
    void uploadWithoutAStreamIsRefusedBeforeTouchingStorage() {
        InputContentFile file = upload(LOGO, null);

        assertThatThrownBy(() -> facade.addLogo(STORE, file)).isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(assets);
    }

    @Test
    void uploadForUnknownStoreIsNotFound() {
        assertThatThrownBy(() -> facade.addStoreLogo(UNKNOWN, upload(LOGO, stream())))
                .isInstanceOf(MerchantStoreNotFoundException.class);
        verifyNoInteractions(assets);
    }

    @Test
    void failedUploadStillClosesTheStreamAndSurfacesTheFailure() throws AssetUploadFailedException {
        TrackedStream stream = stream();
        InputContentFile file = upload(SLIDE, stream);
        doThrow(uploadFailure())
                .when(assets).addFile(any(), any(), any());

        assertThatThrownBy(() -> facade.addSlider(STORE, file)).isInstanceOf(AssetUploadFailedException.class);

        assertThat(stream.closed).isTrue();
        assertThat(file.getFileContentType()).isEqualTo(FileContentType.SLIDER);
    }

    @Test
    void aFailedSliderUploadLeavesTheStoreUntouched() throws AssetUploadFailedException {
        stored();
        doThrow(uploadFailure())
                .when(assets).addFile(any(), any(), any());

        assertThatThrownBy(() -> facade.addStoreSliderImage(STORE, upload(SLIDE, stream())))
                .isInstanceOf(AssetUploadFailedException.class);

        verify(service, never()).save(any());
    }

    @Test
    void aStreamThatWillNotCloseDoesNotFailTheUpload() throws AssetUploadFailedException {
        InputContentFile file = upload(BANNER, new InputStream() {
            @Override
            public int read() {
                return -1;
            }

            @Override
            public void close() throws IOException {
                throw new IOException("stuck");
            }
        });

        facade.addBanner(STORE, file);

        ArgumentCaptor<InputContentFile> sent = ArgumentCaptor.forClass(InputContentFile.class);
        verify(assets).addFile(eq(STORE.getId()), eq(Optional.empty()), sent.capture());
        assertThat(sent.getValue().getFileContentType()).isEqualTo(FileContentType.BANNER);
    }

    private static AssetUploadFailedException uploadFailure() {
        return AssetUploadFailedException.of("files/x", new IOException("storage down"));
    }

    /**
     * Records whether the facade closed it, which the contract requires after success and failure alike.
     */
    private static final class TrackedStream extends ByteArrayInputStream {

        private boolean closed;

        TrackedStream() {
            super(new byte[] {1, 2, 3});
        }

        @Override
        public void close() throws IOException {
            closed = true;
            super.close();
        }

    }

}
