package com.asrevo.cvhome.catalog.service.populator.manufacturer;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

import com.asrevo.cvhome.catalog.entity.product.manufacturer.Manufacturer;
import com.asrevo.cvhome.catalog.errors.ManufacturerNotConvertibleException;
import com.asrevo.cvhome.catalog.model.manufacturer.ManufacturerDescription;
import com.asrevo.cvhome.catalog.model.manufacturer.PersistableManufacturer;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.populator.AbstractDataPopulator;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Carl Samson
 */
@Setter
@Getter
public class PersistableManufacturerPopulator
        extends AbstractDataPopulator<PersistableManufacturer, StoreMerchantId, Manufacturer> {

    @Override
    public Manufacturer populate(PersistableManufacturer source, Manufacturer target, StoreMerchantId store,
                                 LanguageCode language) throws ManufacturerNotConvertibleException {

        try {

            target.setStoreMerchantId(store);
            target.setCode(source.getCode());

            if (!CollectionUtils.isEmpty(source.getDescriptions())) {
                Set<com.asrevo.cvhome.catalog.entity.product.manufacturer.ManufacturerDescription> descriptions = new HashSet<>();
                for (ManufacturerDescription description : source.getDescriptions()) {
                    com.asrevo.cvhome.catalog.entity.product.manufacturer.ManufacturerDescription desc =
                            resolveDescription(description, target);

                    desc.setManufacturer(target);
                    desc.setDescription(description.getDescription());
                    desc.setName(description.getName());
                    desc.setLanguageCode(description.getLanguage());
                    descriptions.add(desc);
                }
                target.setDescriptions(descriptions);
            }

        } catch (Exception e) {
            throw ManufacturerNotConvertibleException.of(e);
        }

        return target;
    }

    private com.asrevo.cvhome.catalog.entity.product.manufacturer.ManufacturerDescription resolveDescription(
            ManufacturerDescription description, Manufacturer target) {
        com.asrevo.cvhome.catalog.entity.product.manufacturer.ManufacturerDescription desc =
                new com.asrevo.cvhome.catalog.entity.product.manufacturer.ManufacturerDescription();
        if (desc.getId() != null && desc.getId() > 0) {
            desc.setId(description.getId());
        }
        if (target.getDescriptions() == null) {
            return desc;
        }
        for (com.asrevo.cvhome.catalog.entity.product.manufacturer.ManufacturerDescription d : target.getDescriptions()) {
            if (d.getLanguageCode().equals(description.getLanguage())
                    || desc.getId() != null && d.getId().longValue() == desc.getId().longValue()) {
                desc = d;
            }
        }
        return desc;
    }

    @Override
    protected Manufacturer createTarget() {

        return null;
    }

}
