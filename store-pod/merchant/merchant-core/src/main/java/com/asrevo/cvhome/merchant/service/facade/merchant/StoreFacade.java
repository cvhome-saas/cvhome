package com.asrevo.cvhome.merchant.service.facade.merchant;

import java.util.List;
import java.util.Set;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.SliderImage;
import com.asrevo.cvhome.commons.domain.SocialLink;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.entity.merchant.MerchantStore;
import com.asrevo.cvhome.merchant.errors.DefaultStoreNotRemovableException;
import com.asrevo.cvhome.merchant.errors.DuplicateMerchantStoreException;
import com.asrevo.cvhome.merchant.errors.MerchantStoreNotFoundException;
import com.asrevo.cvhome.merchant.model.merchant.PersistableMerchantStore;
import com.asrevo.cvhome.merchant.model.merchant.ReadableMerchantStore;
import com.asrevo.cvhome.store.core.entity.content.InputContentFile;
import com.asrevo.cvhome.store.core.modules.cms.errors.AssetUploadFailedException;

/**
 * Layer between shop controllers, services and API with sm-core
 *
 * @author carlsamson
 */
public interface StoreFacade {

    MerchantStore get(StoreMerchantId storeMerchantId);

    List<LanguageCode> supportedLanguages(StoreMerchantId storeMerchantId);

    ReadableMerchantStore getReadableMerchantStoreId(StoreMerchantId storeMerchantId);

    ReadableMerchantStore getByMerchantStoreId(StoreMerchantId storeMerchantId, LanguageCode lang)
            throws MerchantStoreNotFoundException;

    /**
     * Creates a brand new MerchantStore
     */
    void create(PersistableMerchantStore store) throws DuplicateMerchantStoreException;

    /**
     * Deletes a MerchantStore based on store code
     */
    void delete(StoreMerchantId storeMerchantId)
            throws DefaultStoreNotRemovableException, MerchantStoreNotFoundException;

    /**
     * Add MerchantStore logo
     */
    void addStoreLogo(StoreMerchantId storeMerchantId, InputContentFile cmsContentImage);

    /**
     * Add MerchantStore banner
     */
    void addStoreBanner(StoreMerchantId storeMerchantId, InputContentFile cmsContentImage);

    /**
     * Add MerchantStore banner
     *
     * @return
     */
    SliderImage addStoreSliderImage(StoreMerchantId storeMerchantId, InputContentFile cmsContentImage);

    void update(PersistableMerchantStore store) throws MerchantStoreNotFoundException;

    void addLogo(String s, InputContentFile content) throws AssetUploadFailedException;

    void addBanner(String s, InputContentFile content) throws AssetUploadFailedException;

    void addSlider(String s, InputContentFile content) throws AssetUploadFailedException;

    void updateSocialLinks(StoreMerchantId id, Set<SocialLink> socialLinks);

    void updateSliderImages(StoreMerchantId id, List<SliderImage> sliderImages);

}
