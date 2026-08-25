package com.asrevo.cvhome.merchant.service.populator.merchant;

import java.util.List;

import org.springframework.stereotype.Component;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.SliderImage;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.entity.merchant.MerchantStore;
import com.asrevo.cvhome.merchant.model.merchant.PersistableMerchantStore;
import com.asrevo.cvhome.merchant.services.merchant.MerchantStoreService;
import com.asrevo.cvhome.store.core.populator.AbstractDataPopulator;
import com.asrevo.cvhome.store.model.references.PersistableBaseAddress;

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

    /**
     * Narrows the inherited two-argument form, which declares the shared category base so that a migrated populator
     * can name its own conditions. This one maps validated request data onto an entity and cannot fail, so it declares
     * nothing.
     */
    @Override
    public MerchantStore populate(PersistableMerchantStore source, MerchantStore store, LanguageCode language) {
        return populate(source, createTarget(), store, language);
    }

    @Override
    public MerchantStore populate(PersistableMerchantStore source, MerchantStore target, MerchantStore store,
                                  LanguageCode language) {

        if (target == null) {
            target = new MerchantStore();
        }

        applyBasics(source, target, store);
        applyLanguages(source, target);
        applyAddress(source, target);

        if (source.getTemplate() != null && !source.getTemplate().trim().isEmpty()) {
            target.setStoreTemplate(source.getTemplate());
        }

        return target;
    }

    private void applyBasics(PersistableMerchantStore source, MerchantStore target, MerchantStore store) {
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
    }

    /**
     * Declares no failure: every statement here is a null-check and a setter over already-validated request data. The
     * blanket {@code catch (Exception) -> ConversionException} this replaces reported an NPE in our own mapping code
     * as a 400, telling the seller their input was wrong when the fault was ours.
     */
    private void applyLanguages(PersistableMerchantStore source, MerchantStore target) {

        if (source.getDefaultLanguage().code() != null && !source.getDefaultLanguage().code().trim().isEmpty()) {
            target.setDefaultLanguageCode(source.getDefaultLanguage());
        }

        target.setCurrency(source.getCurrency());

        List<LanguageCode> languages = source.getSupportedLanguages().stream().map(LanguageCode::new).toList();
        for (LanguageCode lang : languages) {
            target.getLanguages().add(lang);
        }
    }

    private void applyAddress(PersistableMerchantStore source, MerchantStore target) {
        // address population
        PersistableBaseAddress address = source.getAddress();
        if (address == null) {
            return;
        }
        target.setZone(address.getStateProvince());
        target.setStorestateprovince(address.getStateProvince());
        target.setStoreaddress(address.getAddress());
        target.setStorecity(address.getCity());
        target.setCountry(address.getCountry());
        target.setStorepostalcode(address.getPostalCode());
    }

    @Override
    protected MerchantStore createTarget() {

        return null;
    }

}
