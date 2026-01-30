package com.asrevo.cvhome.merchant.services.merchant;

import com.asrevo.cvhome.commons.domain.SliderImage;
import com.asrevo.cvhome.commons.domain.SocialLink;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.entity.merchant.MerchantStore;
import com.asrevo.cvhome.merchant.repositories.merchant.MerchantRepository;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;

import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("merchantService")
public class MerchantStoreServiceImpl extends SalesManagerEntityServiceImpl<StoreMerchantId, MerchantStore>
		implements MerchantStoreService {

	private final MerchantRepository merchantRepository;

	@Autowired
	public MerchantStoreServiceImpl(MerchantRepository merchantRepository) {
		super(merchantRepository);
		this.merchantRepository = merchantRepository;
	}

	@Override
	public void saveOrUpdate(MerchantStore store) {
		super.save(store);
	}

	@Override
	public void updateSocialLinks(StoreMerchantId id, Set<SocialLink> socialLinks) {
		MerchantStore store = merchantRepository.findByMerchantStoreId(id);
		store.setSocialLinks(socialLinks);
		saveOrUpdate(store);
	}

	@Override
	public void updateSliderImages(StoreMerchantId id, List<SliderImage> sliderImages) {
		MerchantStore store = merchantRepository.findByMerchantStoreId(id);
		store.setSliderImages(sliderImages);
		saveOrUpdate(store);
	}

	@Override
	public MerchantStore getByMerchantStoreId(StoreMerchantId storeMerchantId) {
		return merchantRepository.findByMerchantStoreId(storeMerchantId);
	}

}
