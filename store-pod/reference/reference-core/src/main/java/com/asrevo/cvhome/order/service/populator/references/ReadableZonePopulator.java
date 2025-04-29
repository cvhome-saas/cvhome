package com.asrevo.cvhome.order.service.populator.references;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.order.entity.reference.zone.Zone;
import com.asrevo.cvhome.order.entity.reference.zone.ZoneDescription;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.core.populator.AbstractDataPopulator;
import com.asrevo.cvhome.store.model.references.ReadableZone;
import java.util.Objects;

public class ReadableZonePopulator
        extends AbstractDataPopulator<Zone, StoreMerchantId, ReadableZone> {

    @Override
    public ReadableZone populate(
            Zone source, ReadableZone target, StoreMerchantId store, LanguageCode language) {
        if (target == null) {
            target = new ReadableZone();
        }

        target.setCode(source.getCode());
        target.setCountryCode(source.getCountry());

        if (source.getDescriptions() != null && !source.getDescriptions().isEmpty()) {
            for (ZoneDescription d : source.getDescriptions()) {
                if (Objects.equals(d.getLanguageCode(), language)) {
                    target.setName(d.getName());
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
