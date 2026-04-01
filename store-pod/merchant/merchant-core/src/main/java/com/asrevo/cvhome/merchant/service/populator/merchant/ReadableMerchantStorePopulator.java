package com.asrevo.cvhome.merchant.service.populator.merchant;

import com.asrevo.cvhome.commons.domain.ReadableSliderImage;
import com.asrevo.cvhome.merchant.entity.merchant.MerchantStore;
import com.asrevo.cvhome.merchant.model.merchant.ReadableMerchantStore;
import com.asrevo.cvhome.store.core.model.content.ReadableImage;
import com.asrevo.cvhome.store.core.model.entity.ReadableAudit;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.core.populator.AbstractDataPopulator;
import com.asrevo.cvhome.store.model.references.MeasureUnit;
import com.asrevo.cvhome.store.model.references.ReadableAddress;
import com.asrevo.cvhome.store.model.references.ReadableLanguage;
import com.asrevo.cvhome.store.model.references.WeightUnit;
import com.asrevo.cvhome.store.utils.DateUtil;
import com.asrevo.cvhome.store.utils.ImageFilePath;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Populates MerchantStore core entity model object
 *
 * @author carlsamson
 */
@Component
public class ReadableMerchantStorePopulator
		extends AbstractDataPopulator<MerchantStore, MerchantStore, ReadableMerchantStore> {

	private final ImageFilePath filePath;

	public ReadableMerchantStorePopulator(ImageFilePath filePath) {
		this.filePath = filePath;
	}

	@Override
	public ReadableMerchantStore populate(MerchantStore source, ReadableMerchantStore target, MerchantStore store,
			LanguageCode language) {

		if (target == null) {
			target = new ReadableMerchantStore();
		}

		target.setId(source.getId().getId());
		target.setDefaultLanguage(source.getDefaultLanguageCode());
		target.setCountryIsoCode(source.getCountry());

		target.setCurrency(source.getCurrency());
		target.setPhone(source.getStorephone());

		ReadableAddress address = new ReadableAddress();
		address.setAddress(source.getStoreaddress());
		address.setCity(source.getStorecity());
		if (source.getCountry() != null) {
			address.setCountry(source.getCountry());
		}

		target.setDimension(MeasureUnit.valueOf(source.getSeizeunitcode()));
		target.setWeight(WeightUnit.valueOf(source.getWeightunitcode()));

		if (source.getZone() != null) {
			address.setStateProvince(source.getZone());
			address.setStateProvince(source.getZone());
		}

		if (source.getStorestateprovince() != null) {
			address.setStateProvince(source.getStorestateprovince());
		}

		if (source.getStoreLogo() != null && !source.getStoreLogo().trim().isEmpty()) {
			ReadableImage image = new ReadableImage();
			image.setName(source.getStoreLogo());
			if (filePath != null) {
				image.setPath(filePath.buildStoreLogoFilePath(source.getId(), source.getStoreLogo()));
			}
			target.setLogo(image);
		}

		if (source.getStoreBanner() != null && !source.getStoreBanner().trim().isEmpty()) {
			ReadableImage image = new ReadableImage();
			image.setName(source.getStoreBanner());
			if (filePath != null) {
				image.setPath(filePath.buildStoreBannerFilePath(source.getId(), source.getStoreBanner()));
			}
			target.setBanner(image);
		}

		address.setPostalCode(source.getStorepostalcode());

		target.setAddress(address);

		target.setCurrencyFormatNational(source.isCurrencyFormatNational());
		target.setEmail(source.getStoreEmailAddress());
		target.setName(source.getStorename());
		target.setOrg(source.getOrg());
		target.setTheme(source.getTheme());
		target.setColorTheme(source.getColorTheme());
		target.setId(source.getId().getId());
		target.setInBusinessSince(DateUtil.formatDate(source.getInBusinessSince()));
		target.setUseCache(source.isUseCache());
		target.setRequireLoginForOrderPlacement(source.isRequireLoginForOrderPlacement());

		if (source.getSliderImages() != null && !source.getSliderImages().isEmpty()) {
			Set<ReadableSliderImage> images = source.getSliderImages()
				.stream()
				.map(it -> new ReadableSliderImage(it.priority(), it.name(),
						filePath.buildStoreSliderFilePath(source.getId(), it.name())))
				.collect(Collectors.toSet());
			target.setSliderImages(images);
		}
		target.setSocialLinks(source.getSocialLinks());

		target.setStoreDomains(source.getStoreDomains());

		if (source.getLanguages() != null && !source.getLanguages().isEmpty()) {
			List<ReadableLanguage> supported = new ArrayList<>();
			for (LanguageCode lang : source.getLanguages()) {
				ReadableLanguage l = new ReadableLanguage();
				l.setId(lang);
				l.setCode(lang);
				supported.add(l);
			}
			target.setSupportedLanguages(supported.stream()
				.map(ReadableLanguage::getCode)
				.map(LanguageCode::code)
				.collect(Collectors.toList()));
		}

		if (source.getAuditSection() != null) {
			ReadableAudit audit = new ReadableAudit();
			if (source.getAuditSection().getDateCreated() != null) {
				audit.setCreated(DateUtil.formatDate(source.getAuditSection().getDateCreated()));
			}
			if (source.getAuditSection().getDateModified() != null) {
				audit.setModified(DateUtil.formatDate(source.getAuditSection().getDateCreated()));
			}
			audit.setUser(source.getAuditSection().getModifiedBy());
			target.setReadableAudit(audit);
		}

		return target;
	}

	@Override
	protected ReadableMerchantStore createTarget() {
		// TODO Auto-generated method stub
		return null;
	}

}
