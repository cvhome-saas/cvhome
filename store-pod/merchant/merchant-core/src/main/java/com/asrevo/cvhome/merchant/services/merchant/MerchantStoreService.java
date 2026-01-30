package com.asrevo.cvhome.merchant.services.merchant;

import com.asrevo.cvhome.commons.domain.SliderImage;
import com.asrevo.cvhome.commons.domain.SocialLink;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;

import java.util.List;
import java.util.Set;

public interface MerchantStoreService extends SalesManagerEntityService<StoreMerchantId, MerchantStore> {

	MerchantStore getByMerchantStoreId(StoreMerchantId code);

	void saveOrUpdate(MerchantStore store);

	void updateSocialLinks(StoreMerchantId id, Set<SocialLink> socialLinks);

	void updateSliderImages(StoreMerchantId id, List<SliderImage> sliderImages);

}
