package com.asrevo.cvhome.merchant.service.facade.merchant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.commons.domain.DomainType;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.ManagerStoreDomain;
import com.asrevo.cvhome.commons.domain.SliderImage;
import com.asrevo.cvhome.commons.domain.SocialLink;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.entity.merchant.MerchantStore;
import com.asrevo.cvhome.merchant.errors.DefaultStoreNotRemovableException;
import com.asrevo.cvhome.merchant.errors.DuplicateMerchantStoreException;
import com.asrevo.cvhome.merchant.errors.MerchantStoreNotFoundException;
import com.asrevo.cvhome.merchant.model.merchant.PersistableMerchantStore;
import com.asrevo.cvhome.merchant.model.merchant.ReadableMerchantStore;
import com.asrevo.cvhome.merchant.service.populator.merchant.PersistableMerchantStorePopulator;
import com.asrevo.cvhome.merchant.service.populator.merchant.ReadableMerchantStorePopulator;
import com.asrevo.cvhome.merchant.services.merchant.MerchantStoreService;
import com.asrevo.cvhome.store.controller.exception.ServiceRuntimeException;
import com.asrevo.cvhome.store.core.entity.content.FileContentType;
import com.asrevo.cvhome.store.core.entity.content.InputContentFile;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.modules.cms.content.ContentAssetsManager;
import com.asrevo.cvhome.store.core.modules.cms.errors.AssetUploadFailedException;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import static com.asrevo.cvhome.commons.utils.DefaultStoresConstants.DEFAULT_ORG1_STORE1;

@Service("storeFacade")
@Slf4j
public class StoreFacadeImpl implements StoreFacade {

    private final MerchantStoreService merchantStoreService;

    private final PersistableMerchantStorePopulator persistableMerchantStorePopulator;

    private final ReadableMerchantStorePopulator readableMerchantStorePopulator;

    private final ContentAssetsManager assetsManager;

    public StoreFacadeImpl(MerchantStoreService merchantStoreService,
                           PersistableMerchantStorePopulator persistableMerchantStorePopulator,
                           ReadableMerchantStorePopulator readableMerchantStorePopulator, ContentAssetsManager assetsManager) {
        this.merchantStoreService = merchantStoreService;
        this.persistableMerchantStorePopulator = persistableMerchantStorePopulator;
        this.readableMerchantStorePopulator = readableMerchantStorePopulator;
        this.assetsManager = assetsManager;
    }

    @Override
    public MerchantStore get(StoreMerchantId storeMerchantId) {
        return merchantStoreService.getByMerchantStoreId(storeMerchantId);
    }

    @Override
    public ReadableMerchantStore getByMerchantStoreId(StoreMerchantId storeMerchantId, LanguageCode lang)
            throws MerchantStoreNotFoundException {
        MerchantStore store = getMerchantStoreByMerchantStoreId(storeMerchantId);
        return convertMerchantStoreToReadableMerchantStore(store, lang);
    }

    private ReadableMerchantStore convertMerchantStoreToReadableMerchantStore(MerchantStore store,
                                                                              LanguageCode language) {
        ReadableMerchantStore readable = new ReadableMerchantStore();

        // The populator declares no failure and this catch only ever saw unchecked ones, so it turned a bug in our own
        // mapping code into a 400 blaming the caller.
        readableMerchantStorePopulator.populate(store, readable, store, language);
        return readable;
    }

    private MerchantStore getMerchantStoreByMerchantStoreId(StoreMerchantId storeMerchantId)
            throws MerchantStoreNotFoundException {
        return Optional.ofNullable(get(storeMerchantId))
                .orElseThrow(() -> MerchantStoreNotFoundException.of(storeMerchantId));
    }

    @Override
    public void create(PersistableMerchantStore store) throws DuplicateMerchantStoreException {
        MerchantStore storeForCheck = get(new StoreMerchantId(store.getId()));
        if (storeForCheck != null) {
            // Was a 400 that said "MerhantStore ... already exists"; a taken id is a conflict, not a malformed request.
            throw DuplicateMerchantStoreException.of(store.getId());
        }

        MerchantStore mStore = convertPersistableMerchantStoreToMerchantStore(store, LanguageCode.defaultLanguage());
        createMerchantStore(mStore);
    }

    private void createMerchantStore(MerchantStore mStore) {
        merchantStoreService.saveOrUpdate(mStore);
    }

    private MerchantStore convertPersistableMerchantStoreToMerchantStore(PersistableMerchantStore store,
                                                                         LanguageCode language) {
        MerchantStore mStore = new MerchantStore();
        store.setStoreDomains(Set.of(new ManagerStoreDomain(store.getName(), DomainType.SUB_DOMAIN)));

        mStore = persistableMerchantStorePopulator.populate(store, mStore, language);
        return mStore;
    }

    @Override
    public void update(PersistableMerchantStore store) throws MerchantStoreNotFoundException {
        MerchantStore mStore = mergePersistableMerchantStoreToMerchantStore(store, new StoreMerchantId(store.getId()),
                LanguageCode.defaultLanguage());

        updateMerchantStore(mStore);
    }

    private void updateMerchantStore(MerchantStore mStore) {
        try {
            merchantStoreService.update(mStore);
        } catch (ServiceException e) {
            throw new ServiceRuntimeException(e);
        }
    }

    private MerchantStore mergePersistableMerchantStoreToMerchantStore(PersistableMerchantStore store,
                                                                       StoreMerchantId storeMerchantId, LanguageCode language)
            throws MerchantStoreNotFoundException {

        MerchantStore mStore = getMerchantStoreByMerchantStoreId(storeMerchantId);

        store.setId(mStore.getId().getId());
        store.setOrg(mStore.getOrg());

        store.setSocialLinks(mStore.getSocialLinks());
        store.setSliderImages(mStore.getSliderImages());
        store.setStoreDomains(mStore.getStoreDomains());

        mStore = persistableMerchantStorePopulator.populate(store, mStore, language);
        return mStore;
    }

    @Override
    public void delete(StoreMerchantId storeMerchantId)
            throws DefaultStoreNotRemovableException, MerchantStoreNotFoundException {

        if (DEFAULT_ORG1_STORE1.equals(storeMerchantId)) {
            throw DefaultStoreNotRemovableException.of(storeMerchantId);
        }

        MerchantStore mStore = getMerchantStoreByMerchantStoreId(storeMerchantId);

        try {
            merchantStoreService.delete(mStore);
        } catch (Exception e) {
            log.error("Error while deleting MerchantStore", e);
            throw new ServiceRuntimeException(String.format("Error while deleting MerchantStore %s", e.getMessage()));
        }
    }

    private MerchantStore getByMerchantStoreId(StoreMerchantId storeMerchantId)
            throws MerchantStoreNotFoundException {
        return getMerchantStoreByMerchantStoreId(storeMerchantId);
    }

    @SneakyThrows
    @Override
    public void addStoreLogo(StoreMerchantId storeMerchantId, InputContentFile cmsContentImage) {
        MerchantStore store = getByMerchantStoreId(storeMerchantId);
        store.setStoreLogo(cmsContentImage.getFileName());
        saveMerchantStore(store);
        addLogo(storeMerchantId.getId(), cmsContentImage);
    }

    @SneakyThrows
    @Override
    public void addStoreBanner(StoreMerchantId storeMerchantId, InputContentFile cmsContentImage) {
        MerchantStore store = getByMerchantStoreId(storeMerchantId);
        store.setStoreBanner(cmsContentImage.getFileName());
        saveMerchantStore(store);
        addBanner(storeMerchantId.getId(), cmsContentImage);
    }

    @SneakyThrows
    @Override
    public SliderImage addStoreSliderImage(StoreMerchantId storeMerchantId, InputContentFile cmsContentImage) {
        MerchantStore store = getByMerchantStoreId(storeMerchantId);
        List<SliderImage> sliderImages = new ArrayList<>(store.getSliderImages());
        Integer nextPriority = sliderImages.stream()
                .map(SliderImage::priority)
                .max(Comparator.naturalOrder())
                .map(it -> it + 1)
                .orElse(0);
        SliderImage sliderImage = new SliderImage(nextPriority, cmsContentImage.getFileName());
        sliderImages.add(sliderImage);
        store.setSliderImages(sliderImages);
        saveMerchantStore(store);
        addSlider(storeMerchantId.getId(), cmsContentImage);
        return sliderImage;
    }

    @Override
    public void addLogo(String merchantStoreCode, InputContentFile cmsContentImage)
            throws AssetUploadFailedException {
        cmsContentImage.setFileContentType(FileContentType.LOGO);
        addImageToAssets(merchantStoreCode, cmsContentImage);
    }

    @Override
    public void addBanner(String merchantStoreCode, InputContentFile cmsContentImage)
            throws AssetUploadFailedException {
        cmsContentImage.setFileContentType(FileContentType.BANNER);
        addImageToAssets(merchantStoreCode, cmsContentImage);
    }

    @Override
    public void addSlider(String merchantStoreCode, InputContentFile cmsContentImage)
            throws AssetUploadFailedException {
        cmsContentImage.setFileContentType(FileContentType.SLIDER);
        addImageToAssets(merchantStoreCode, cmsContentImage);
    }

    @Override
    public void updateSocialLinks(StoreMerchantId id, Set<SocialLink> socialLinks) {
        merchantStoreService.updateSocialLinks(id, socialLinks);
    }

    @Override
    public void updateSliderImages(StoreMerchantId id, List<SliderImage> sliderImages) {
        merchantStoreService.updateSliderImages(id, sliderImages);
    }

    private void addImageToAssets(String merchantStoreCode, InputContentFile contentImage)
            throws AssetUploadFailedException {

        if (contentImage.getFile() == null) {
            throw new IllegalArgumentException("File is null");
        }
        try {
            log.info("Adding content image for merchant id {}", merchantStoreCode);

            String p = contentImage.getPath();
            Optional<String> path = Optional.ofNullable(p);
            // The blanket catch that used to sit here re-flattened AssetUploadFailedException, which already names the
            // key that failed, back into an untyped ServiceException.
            assetsManager.addFile(merchantStoreCode, path, contentImage);

        } finally {
            try {
                contentImage.getFile().close();
            } catch (IOException e) {
                // The upload has already succeeded or thrown; a failure to close is not the caller's business.
                log.warn("Could not close the upload stream for {}", contentImage.getFileName(), e);
            }
        }
    }

    private void saveMerchantStore(MerchantStore store) {
        merchantStoreService.save(store);
    }

    @Override
    public List<LanguageCode> supportedLanguages(StoreMerchantId storeMerchantId) {
        MerchantStore store = merchantStoreService.getByMerchantStoreId(storeMerchantId);

        if (store != null) {
            return store.getLanguages();
        }

        return Collections.emptyList();
    }

    @Override
    public ReadableMerchantStore getReadableMerchantStoreId(StoreMerchantId storeMerchantId) {
        MerchantStore merchantStore = get(storeMerchantId);
        return convertMerchantStoreToReadableMerchantStore(merchantStore, merchantStore.getDefaultLanguageCode());
    }

}
