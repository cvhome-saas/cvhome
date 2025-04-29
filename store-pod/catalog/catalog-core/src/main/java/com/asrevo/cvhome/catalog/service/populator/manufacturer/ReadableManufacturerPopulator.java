package com.asrevo.cvhome.catalog.service.populator.manufacturer;

import com.asrevo.cvhome.catalog.entity.product.manufacturer.Manufacturer;
import com.asrevo.cvhome.catalog.entity.product.manufacturer.ManufacturerDescription;
import com.asrevo.cvhome.catalog.model.manufacturer.ReadableManufacturer;
import com.asrevo.cvhome.catalog.model.manufacturer.ReadableManufacturerFull;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.core.populator.AbstractDataPopulator;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ReadableManufacturerPopulator
        extends AbstractDataPopulator<Manufacturer, StoreMerchantId, ReadableManufacturer> {

    @Override
    public ReadableManufacturer populate(
            Manufacturer source,
            ReadableManufacturer target,
            StoreMerchantId store,
            LanguageCode language) {

        if (language == null) {
            target = new ReadableManufacturerFull();
        }
        target.setOrder(source.getOrder());
        target.setId(source.getId());
        target.setCode(source.getCode());
        if (source.getDescriptions() != null && !source.getDescriptions().isEmpty()) {

            List<com.asrevo.cvhome.catalog.model.manufacturer.ManufacturerDescription>
                    fulldescriptions = new ArrayList<>();

            Set<ManufacturerDescription> descriptions = source.getDescriptions();
            ManufacturerDescription description = null;
            for (ManufacturerDescription desc : descriptions) {
                if (desc.getLanguageCode().equals(language)) {
                    description = desc;
                    break;
                } else {
                    fulldescriptions.add(populateDescription(desc));
                }
            }

            if (description != null) {
                com.asrevo.cvhome.catalog.model.manufacturer.ManufacturerDescription d =
                        populateDescription(description);
                target.setDescription(d);
            }

            if (target instanceof ReadableManufacturerFull) {
                ((ReadableManufacturerFull) target).setDescriptions(fulldescriptions);
            }
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
        com.asrevo.cvhome.catalog.model.manufacturer.ManufacturerDescription d =
                new com.asrevo.cvhome.catalog.model.manufacturer.ManufacturerDescription();
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
