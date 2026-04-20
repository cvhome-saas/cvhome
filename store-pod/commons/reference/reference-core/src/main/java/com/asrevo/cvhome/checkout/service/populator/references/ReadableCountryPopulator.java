package com.asrevo.cvhome.checkout.service.populator.references;

import java.util.Objects;

import org.springframework.util.CollectionUtils;

import com.asrevo.cvhome.checkout.entity.reference.country.Country;
import com.asrevo.cvhome.checkout.entity.reference.zone.Zone;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.core.populator.AbstractDataPopulator;
import com.asrevo.cvhome.store.model.references.ReadableCountry;
import com.asrevo.cvhome.store.model.references.ReadableZone;

public class ReadableCountryPopulator extends AbstractDataPopulator<Country, StoreMerchantId, ReadableCountry> {

    @Override
    public ReadableCountry populate(Country source, ReadableCountry target, StoreMerchantId store,
                                    LanguageCode language) {

        if (target == null) {
            target = new ReadableCountry();
        }

        target.setCode(source.getIsoCode());
        target.setSupported(source.isSupported());
        if (!CollectionUtils.isEmpty(source.getDescriptions())) {
            target.setName(source.getDescriptions().iterator().next().getName());
        }

        if (!CollectionUtils.isEmpty(source.getZones())) {
            for (Zone z : source.getZones()) {
                ReadableZone readableZone = new ReadableZone();
                readableZone.setCountryCode(target.getCode());
                readableZone.setCode(z.getCode());
                if (!CollectionUtils.isEmpty(z.getDescriptions())) {
                    z.getDescriptions().stream().filter(it -> Objects.equals(it.getLanguageCode(), language))
                            .forEach(it -> readableZone.setName(it.getName()));
                }
                target.getZones().add(readableZone);
            }
        }

        return target;
    }

    @Override
    protected ReadableCountry createTarget() {
        // TODO Auto-generated method stub
        return null;
    }

}
