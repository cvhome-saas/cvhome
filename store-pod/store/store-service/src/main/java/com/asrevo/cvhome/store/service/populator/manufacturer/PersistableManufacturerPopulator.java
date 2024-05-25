package com.asrevo.cvhome.store.service.populator.manufacturer;

import com.asrevo.cvhome.store.controller.exception.ConversionException;
import com.asrevo.cvhome.store.core.entity.catalog.product.manufacturer.Manufacturer;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.catalog.manufacturer.ManufacturerDescription;
import com.asrevo.cvhome.store.core.model.catalog.manufacturer.PersistableManufacturer;
import com.asrevo.cvhome.store.core.services.reference.language.LanguageService;
import com.asrevo.cvhome.store.utils.AbstractDataPopulator;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.util.Assert;

import java.util.HashSet;
import java.util.Set;


/**
 * @author Carl Samson
 */


@Setter
@Getter
public class PersistableManufacturerPopulator extends AbstractDataPopulator<PersistableManufacturer, Manufacturer> {


    private LanguageService languageService;

    @Override
    public Manufacturer populate(PersistableManufacturer source,
                                 Manufacturer target, MerchantStore store, Language language)
            throws ConversionException {

        Assert.notNull(languageService, "Requires to set LanguageService");

        try {

            target.setMerchantStore(store);
            target.setCode(source.getCode());


            if (!CollectionUtils.isEmpty(source.getDescriptions())) {
                Set<com.asrevo.cvhome.store.core.entity.catalog.product.manufacturer.ManufacturerDescription> descriptions = new HashSet<>();
                for (ManufacturerDescription description : source.getDescriptions()) {
                    com.asrevo.cvhome.store.core.entity.catalog.product.manufacturer.ManufacturerDescription desc = new com.asrevo.cvhome.store.core.entity.catalog.product.manufacturer.ManufacturerDescription();
                    if (desc.getId() != null && desc.getId() > 0) {
                        desc.setId(description.getId());
                    }
                    if (target.getDescriptions() != null) {
                        for (com.asrevo.cvhome.store.core.entity.catalog.product.manufacturer.ManufacturerDescription d : target.getDescriptions()) {
                            if (d.getLanguage().getCode().equals(description.getLanguage()) || desc.getId() != null && d.getId().longValue() == desc.getId().longValue()) {
                                desc = d;
                            }
                        }
                    }

                    desc.setManufacturer(target);
                    desc.setDescription(description.getDescription());
                    desc.setName(description.getName());
                    Language lang = languageService.getByCode(description.getLanguage());
                    if (lang == null) {
                        throw new ConversionException("Language is null for code " + description.getLanguage() + " use language ISO code [en, fr ...]");
                    }
                    desc.setLanguage(lang);
                    descriptions.add(desc);
                }
                target.setDescriptions(descriptions);
            }

        } catch (Exception e) {
            throw new ConversionException(e);
        }


        return target;
    }

    @Override
    protected Manufacturer createTarget() {
        // TODO Auto-generated method stub
        return null;
    }


}
