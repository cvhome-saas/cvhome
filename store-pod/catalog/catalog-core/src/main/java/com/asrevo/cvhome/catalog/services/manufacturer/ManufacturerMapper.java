package com.asrevo.cvhome.catalog.services.manufacturer;

import java.util.HashMap;
import java.util.Map;

import com.asrevo.cvhome.catalog.entity.Manufacturer;
import com.asrevo.cvhome.catalog.entity.ManufacturerDescription;
import com.asrevo.cvhome.catalog.model.manufacturer.PersistableManufacturer;
import com.asrevo.cvhome.catalog.model.manufacturer.ReadableManufacturer;
import com.asrevo.cvhome.commons.domain.LanguageCode;

public final class ManufacturerMapper {

    private ManufacturerMapper() {
    }

    public static ReadableManufacturer toReadable(Manufacturer manufacturer, LanguageCode language,
                                                  boolean allLanguages) {
        ReadableManufacturer readable = new ReadableManufacturer();
        readable.setId(manufacturer.getId());
        readable.setCode(manufacturer.getCode());
        readable.setOrder(manufacturer.getOrder());
        if (LanguageCode.isLanguage(language)) {
            manufacturer.description(language).map(ManufacturerMapper::description)
                    .ifPresent(readable::setDescription);
        }
        if (allLanguages) {
            readable.setDescriptions(manufacturer.getDescriptions().stream()
                    .map(ManufacturerMapper::description).toList());
        }
        return readable;
    }

    public static void apply(PersistableManufacturer source, Manufacturer target) {
        target.setCode(source.getCode());
        target.setOrder(source.getOrder());
        Map<LanguageCode, ManufacturerDescription> existing = new HashMap<>();
        target.getDescriptions().forEach(d -> existing.put(d.getLanguageCode(), d));
        target.getDescriptions().clear();
        for (com.asrevo.cvhome.catalog.model.manufacturer.ManufacturerDescription d : source.getDescriptions()) {
            ManufacturerDescription entity = existing.getOrDefault(d.getLanguage(),
                    new ManufacturerDescription(target));
            entity.setLanguageCode(d.getLanguage());
            entity.setName(d.getName());
            entity.setTitle(d.getTitle());
            entity.setDescription(d.getDescription());
            entity.setUrl(d.getFriendlyUrl());
            target.getDescriptions().add(entity);
        }
    }

    private static com.asrevo.cvhome.catalog.model.manufacturer.ManufacturerDescription description(
            ManufacturerDescription d) {
        var readable = new com.asrevo.cvhome.catalog.model.manufacturer.ManufacturerDescription();
        readable.setId(d.getId());
        readable.setLanguage(d.getLanguageCode());
        readable.setName(d.getName());
        readable.setTitle(d.getTitle());
        readable.setDescription(d.getDescription());
        readable.setFriendlyUrl(d.getUrl());
        return readable;
    }
}
