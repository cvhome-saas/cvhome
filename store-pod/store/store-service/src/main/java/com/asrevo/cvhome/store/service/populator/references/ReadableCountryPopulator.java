package com.asrevo.cvhome.store.service.populator.references;


import com.asrevo.cvhome.store.controller.exception.ConversionException;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.country.Country;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.entity.reference.zone.Zone;
import com.asrevo.cvhome.store.core.entity.reference.zone.ZoneDescription;
import com.asrevo.cvhome.store.core.model.references.ReadableCountry;
import com.asrevo.cvhome.store.core.model.references.ReadableZone;
import com.asrevo.cvhome.store.utils.AbstractDataPopulator;
import org.apache.commons.collections.CollectionUtils;

import java.util.Objects;

public class ReadableCountryPopulator extends AbstractDataPopulator<Country, ReadableCountry> {

	@Override
	public ReadableCountry populate(Country source, ReadableCountry target, MerchantStore store, Language language)
			throws ConversionException {
		
		if(target==null) {
			target = new ReadableCountry();
		}
		
		target.setId(source.getId().longValue());
		target.setCode(source.getIsoCode());
		target.setSupported(source.isSupported());
		if(!CollectionUtils.isEmpty(source.getDescriptions())) {
			target.setName(source.getDescriptions().iterator().next().getName());
	    }
		
		if(!CollectionUtils.isEmpty(source.getZones())) {
			for(Zone z : source.getZones()) {
				ReadableZone readableZone = new ReadableZone();
				readableZone.setCountryCode(target.getCode());
				readableZone.setId(z.getId());
				if(!CollectionUtils.isEmpty(z.getDescriptions())) {
					for(ZoneDescription d : z.getDescriptions()) {
						if(Objects.equals(d.getLanguage().getId(), language.getId())) {
							readableZone.setName(d.getName());
							continue;
						}
					}
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
