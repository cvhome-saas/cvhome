package com.asrevo.cvhome.merchant.services.merchant;

import java.util.List;
import java.util.Set;

import com.asrevo.cvhome.commons.domain.SliderImage;
import com.asrevo.cvhome.commons.domain.SocialLink;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;

public interface MerchantStoreService extends SalesManagerEntityService<StoreMerchantId, MerchantStore> {

    MerchantStore getByMerchantStoreId(StoreMerchantId code);

    void saveOrUpdate(MerchantStore store);

    void updateSocialLinks(StoreMerchantId id, Set<SocialLink> socialLinks);

    void updateSliderImages(StoreMerchantId id, List<SliderImage> sliderImages);

}
