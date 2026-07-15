package com.asrevo.cvhome.catalog.service.populator.manufacturer;

import java.util.Optional;
import java.util.Set;

import com.asrevo.cvhome.catalog.entity.product.manufacturer.Manufacturer;
import com.asrevo.cvhome.catalog.entity.product.manufacturer.ManufacturerDescription;
import com.asrevo.cvhome.catalog.model.manufacturer.ReadableManufacturer;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.store.core.populator.AbstractDataPopulator;

public class ReadableManufacturerPopulator
        extends AbstractDataPopulator<Manufacturer, StoreMerchantId, ReadableManufacturer> {

    @Override
    public ReadableManufacturer populate(Manufacturer source, ReadableManufacturer target, StoreMerchantId store,
                                         LanguageCode language) {

        if (target == null) {
            target = new ReadableManufacturer();
        }
        target.setOrder(source.getOrder());
        target.setId(source.getId());
        target.setCode(source.getCode());

        if (LanguageCode.isAllLanguage(language)) {
            var descriptionSet = Optional.ofNullable(source.getDescriptions()).orElse(Set.of());
            target.setDescriptions(descriptionSet.stream().map(this::populateDescription).toList());
        }
        if (LanguageCode.isLanguage(language)) {
            var descriptionSet = Optional.ofNullable(source.getDescriptions()).orElse(Set.of());
            var description = descriptionSet.stream()
                    .filter(it -> language.equals(it.getLanguageCode()))
                    .findFirst()
                    .map(this::populateDescription)
                    .orElse(null);
            target.setDescription(description);
        }

        return target;
    }

    @Override
    protected ReadableManufacturer createTarget() {
        return null;
    }

    com.asrevo.cvhome.catalog.model.manufacturer.ManufacturerDescription populateDescription(
            ManufacturerDescription description) {
        if (description == null) {
            return null;
        }
        var d = new com.asrevo.cvhome.catalog.model.manufacturer.ManufacturerDescription();
        d.setName(description.getName());
        d.setDescription(description.getDescription());
        d.setId(description.getId());
        d.setTitle(description.getTitle());
        if (description.getLanguageCode() != null) {
            d.setLanguage(description.getLanguageCode());
        }
        return d;
    }

}
