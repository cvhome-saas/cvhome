package com.asrevo.cvhome.merchant.service.populator.merchant;

import java.util.List;

import org.springframework.stereotype.Component;

import com.asrevo.cvhome.commons.domain.SliderImage;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.entity.merchant.MerchantStore;
import com.asrevo.cvhome.merchant.model.merchant.PersistableMerchantStore;
import com.asrevo.cvhome.merchant.services.merchant.MerchantStoreService;
import com.asrevo.cvhome.store.core.exception.ConversionException;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.core.populator.AbstractDataPopulator;
import com.asrevo.cvhome.store.model.references.PersistableAddress;

import lombok.Getter;
import lombok.Setter;

@Component
@Getter
@Setter
public class PersistableMerchantStorePopulator
        extends AbstractDataPopulator<PersistableMerchantStore, MerchantStore, MerchantStore> {

    private final MerchantStoreService merchantStoreService;

    public PersistableMerchantStorePopulator(MerchantStoreService merchantStoreService) {
        this.merchantStoreService = merchantStoreService;
    }

    @Override
    public MerchantStore populate(PersistableMerchantStore source, MerchantStore target, MerchantStore store,
                                  LanguageCode language) throws ConversionException {

        if (target == null) {
            target = new MerchantStore();
        }

        if (source.getId() != null) {
            target.setId(new StoreMerchantId(source.getId()));
        }

        if (store.getStoreLogo() != null) {
            target.setStoreLogo(store.getStoreLogo());
        }

        if (store.getStoreBanner() != null) {
            target.setStoreBanner(store.getStoreBanner());
        }

        target.setInBusinessSince(source.getInBusinessSince());

        if (source.getDimension() != null) {
            target.setSeizeunitcode(source.getDimension().name());
        }
        if (source.getWeight() != null) {
            target.setWeightunitcode(source.getWeight().name());
        }
        target.setCurrencyFormatNational(source.isCurrencyFormatNational());
        target.setStorename(source.getName());
        if (source.getOrg() != null) {
            target.setOrg(source.getOrg());
        }
        target.setTheme(source.getTheme());
        target.setColorTheme(source.getColorTheme());
        if (source.getSocialLinks() != null) {
            target.setSocialLinks(source.getSocialLinks());
        }
        if (source.getStoreDomains() != null) {
            target.setStoreDomains(source.getStoreDomains());
        }
        if (source.getSliderImages() != null) {
            List<SliderImage> sliderImages = source.getSliderImages()
                    .stream()
                    .map(it -> new SliderImage(it.priority(), it.name()))
                    .toList();
            target.setSliderImages(sliderImages);
        }
        target.setStorephone(source.getPhone());
        target.setStoreEmailAddress(source.getEmail());
        target.setUseCache(source.isUseCache());
        target.setRequireLoginForOrderPlacement(source.isRequireLoginForOrderPlacement());

        try {

            if (source.getDefaultLanguage().code() != null && !source.getDefaultLanguage().code().trim().isEmpty()) {
                target.setDefaultLanguageCode(source.getDefaultLanguage());
            }

            target.setCurrency(source.getCurrency());

            List<LanguageCode> languages = source.getSupportedLanguages().stream().map(LanguageCode::new).toList();
            if (!languages.isEmpty()) {
                for (LanguageCode lang : languages) {
                    target.getLanguages().add(lang);
                }
            }

        } catch (Exception e) {
            throw new ConversionException(e);
        }

        // address population
        PersistableAddress address = source.getAddress();
        if (address != null) {
            target.setZone(address.getStateProvince());
            target.setStorestateprovince(address.getStateProvince());
            target.setStoreaddress(address.getAddress());
            target.setStorecity(address.getCity());
            target.setCountry(address.getCountry());
            target.setStorepostalcode(address.getPostalCode());
        }

        if (source.getTemplate() != null && !source.getTemplate().trim().isEmpty()) {
            target.setStoreTemplate(source.getTemplate());
        }

        return target;
    }

    @Override
    protected MerchantStore createTarget() {

        return null;
    }

}
