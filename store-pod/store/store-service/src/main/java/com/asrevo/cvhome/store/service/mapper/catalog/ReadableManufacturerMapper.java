package com.asrevo.cvhome.store.service.mapper.catalog;

import com.asrevo.cvhome.store.core.entity.catalog.product.manufacturer.Manufacturer;
import com.asrevo.cvhome.store.core.entity.catalog.product.manufacturer.ManufacturerDescription;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.catalog.manufacturer.ReadableManufacturer;
import com.asrevo.cvhome.store.service.mapper.Mapper;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

@Component
public class ReadableManufacturerMapper implements Mapper<Manufacturer, ReadableManufacturer> {

    @Override
    public ReadableManufacturer convert(Manufacturer source, MerchantStore store, Language language) {

        if (language == null) {
            language = store.getDefaultLanguage();
        }
        ReadableManufacturer target = new ReadableManufacturer();

        Optional<com.asrevo.cvhome.store.core.model.catalog.manufacturer.ManufacturerDescription> description =
                getDescription(source, language, target);
        description.ifPresent(target::setDescription);

        target.setCode(source.getCode());
        target.setId(source.getId());
        target.setOrder(source.getOrder());
        Optional<com.asrevo.cvhome.store.core.model.catalog.manufacturer.ManufacturerDescription> desc = this.getDescription(source, language, target);
        if (description.isPresent()) {
            target.setDescription(desc.get());
        }


        return target;
    }

    private Optional<com.asrevo.cvhome.store.core.model.catalog.manufacturer.ManufacturerDescription> getDescription(
            Manufacturer source, Language language, ReadableManufacturer target) {

        Optional<ManufacturerDescription> description =
                getDescription(source.getDescriptions(), language);
        if (source.getDescriptions() != null && !source.getDescriptions().isEmpty()
                && description.isPresent()) {
            return Optional.of(convertDescription(description.get(), source));
        } else {
            return Optional.empty();
        }
    }

    private Optional<ManufacturerDescription> getDescription(
            Set<ManufacturerDescription> descriptionsLang, Language language) {
        Optional<ManufacturerDescription> descriptionByLang = descriptionsLang.stream()
                .filter(desc -> desc.getLanguage().getCode().equals(language.getCode())).findAny();
        if (descriptionByLang.isPresent()) {
            return descriptionByLang;
        } else {
            return Optional.empty();
        }
    }

    private com.asrevo.cvhome.store.core.model.catalog.manufacturer.ManufacturerDescription convertDescription(
            ManufacturerDescription description, Manufacturer source) {
        final com.asrevo.cvhome.store.core.model.catalog.manufacturer.ManufacturerDescription desc =
                new com.asrevo.cvhome.store.core.model.catalog.manufacturer.ManufacturerDescription();

        desc.setFriendlyUrl(description.getUrl());
        desc.setId(description.getId());
        desc.setLanguage(description.getLanguage().getCode());
        desc.setName(description.getName());
        desc.setDescription(description.getDescription());
        return desc;
    }

    @Override
    public ReadableManufacturer merge(Manufacturer source, ReadableManufacturer destination,
                                      MerchantStore store, Language language) {
        return destination;
    }

}
