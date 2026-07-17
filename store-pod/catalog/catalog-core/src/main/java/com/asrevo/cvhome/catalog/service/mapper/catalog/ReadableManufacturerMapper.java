package com.asrevo.cvhome.catalog.service.mapper.catalog;

import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.asrevo.cvhome.catalog.entity.product.manufacturer.Manufacturer;
import com.asrevo.cvhome.catalog.entity.product.manufacturer.ManufacturerDescription;
import com.asrevo.cvhome.catalog.model.manufacturer.ReadableManufacturer;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.store.core.mapper.Mapper;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class ReadableManufacturerMapper implements Mapper<Manufacturer, ReadableManufacturer> {

    private final ExternalMerchantStoreService externalMerchantStoreService;

    @Override
    public ReadableManufacturer convert(Manufacturer source, StoreMerchantId store, LanguageCode language) {

        if (language == null) {
            language = externalMerchantStoreService.getStore(store).getDefaultLanguage();
        }

        ReadableManufacturer target = new ReadableManufacturer();

        Optional<com.asrevo.cvhome.catalog.model.manufacturer.ManufacturerDescription> description = getDescription(
                source, language);
        description.ifPresent(target::setDescription);

        target.setCode(source.getCode());
        target.setId(source.getId());
        target.setOrder(source.getOrder());
        Optional<com.asrevo.cvhome.catalog.model.manufacturer.ManufacturerDescription> desc = this
                .getDescription(source, language);
        if (description.isPresent()) {
            target.setDescription(desc.orElse(null));
        }

        return target;
    }

    private Optional<com.asrevo.cvhome.catalog.model.manufacturer.ManufacturerDescription> getDescription(
            Manufacturer source, LanguageCode language) {

        Optional<ManufacturerDescription> description = getDescription(source.getDescriptions(), language);
        if (source.getDescriptions() != null && !source.getDescriptions().isEmpty() && description.isPresent()) {
            return Optional.of(convertDescription(description.get()));
        } else {
            return Optional.empty();
        }
    }

    private Optional<ManufacturerDescription> getDescription(Set<ManufacturerDescription> descriptionsLang,
                                                             LanguageCode language) {
        return descriptionsLang.stream().filter(desc -> desc.getLanguageCode().equals(language)).findAny();
    }

    private com.asrevo.cvhome.catalog.model.manufacturer.ManufacturerDescription convertDescription(
            ManufacturerDescription description) {
        final com.asrevo.cvhome.catalog.model.manufacturer.ManufacturerDescription desc =
                new com.asrevo.cvhome.catalog.model.manufacturer.ManufacturerDescription();

        desc.setFriendlyUrl(description.getUrl());
        desc.setId(description.getId());
        desc.setLanguage(description.getLanguageCode());
        desc.setName(description.getName());
        desc.setDescription(description.getDescription());
        return desc;
    }

    @Override
    public ReadableManufacturer merge(Manufacturer source, ReadableManufacturer destination, StoreMerchantId store,
                                      LanguageCode language) {
        return destination;
    }

}
