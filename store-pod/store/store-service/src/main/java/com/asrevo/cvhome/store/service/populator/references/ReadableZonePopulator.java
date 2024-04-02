package com.asrevo.cvhome.store.service.populator.references;


import com.asrevo.cvhome.store.controller.exception.ConversionException;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.entity.reference.zone.Zone;
import com.asrevo.cvhome.store.core.entity.reference.zone.ZoneDescription;
import com.asrevo.cvhome.store.core.model.references.ReadableZone;
import com.asrevo.cvhome.store.utils.AbstractDataPopulator;
import org.apache.commons.collections.CollectionUtils;

import java.util.Objects;

public class ReadableZonePopulator extends AbstractDataPopulator<Zone, ReadableZone> {

    @Override
    public ReadableZone populate(Zone source, ReadableZone target, MerchantStore store, Language language)
            throws ConversionException {
        if (target == null) {
            target = new ReadableZone();
        }

        target.setId(source.getId());
        target.setCode(source.getCode());
        target.setCountryCode(source.getCountry().getIsoCode());

        if (!CollectionUtils.isEmpty(source.getDescriptions())) {
            for (ZoneDescription d : source.getDescriptions()) {
                if (Objects.equals(d.getLanguage().getId(), language.getId())) {
                    target.setName(d.getName());
                    continue;
                }
            }
        }

        return target;

    }

    @Override
    protected ReadableZone createTarget() {
        // TODO Auto-generated method stub
        return null;
    }

}
