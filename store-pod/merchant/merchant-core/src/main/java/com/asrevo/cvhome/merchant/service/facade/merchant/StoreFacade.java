package com.asrevo.cvhome.merchant.service.facade.merchant;

import com.asrevo.cvhome.commons.domain.SliderImage;
import com.asrevo.cvhome.commons.domain.SocialLink;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.entity.merchant.MerchantStore;
import com.asrevo.cvhome.merchant.model.merchant.PersistableMerchantStore;
import com.asrevo.cvhome.merchant.model.merchant.ReadableMerchantStore;
import com.asrevo.cvhome.store.core.entity.content.InputContentFile;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import java.util.List;
import java.util.Set;

/**
 * Layer between shop controllers, services and API with sm-core
 *
 * @author carlsamson
 */
public interface StoreFacade {
    MerchantStore get(StoreMerchantId storeMerchantId);

    List<LanguageCode> supportedLanguages(StoreMerchantId storeMerchantId);

    ReadableMerchantStore getReadableMerchantStoreId(StoreMerchantId storeMerchantId);

    ReadableMerchantStore getByMerchantStoreId(StoreMerchantId storeMerchantId, LanguageCode lang);

    /**
     * Creates a brand new MerchantStore
     */
    // ReadableMerchantStore create(PersistableMerchantStore store);
    void create(PersistableMerchantStore store);

    /**
     * Deletes a MerchantStore based on store code
     */
    void delete(StoreMerchantId storeMerchantId);

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
    SliderImage addStoreSliderImage(
            StoreMerchantId storeMerchantId, InputContentFile cmsContentImage);

    void update(PersistableMerchantStore store);

    void addLogo(String s, InputContentFile content) throws ServiceException;

    void addBanner(String s, InputContentFile content) throws ServiceException;

    void addSlider(String s, InputContentFile content) throws ServiceException;

    void updateSocialLinks(StoreMerchantId id, Set<SocialLink> socialLinks);

    void updateSliderImages(StoreMerchantId id, Set<SliderImage> sliderImages);
}
