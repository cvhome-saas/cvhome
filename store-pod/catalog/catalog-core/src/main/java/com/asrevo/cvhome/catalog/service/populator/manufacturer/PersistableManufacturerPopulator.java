package com.asrevo.cvhome.catalog.service.populator.manufacturer;

import com.asrevo.cvhome.catalog.entity.product.manufacturer.Manufacturer;
import com.asrevo.cvhome.catalog.model.manufacturer.ManufacturerDescription;
import com.asrevo.cvhome.catalog.model.manufacturer.PersistableManufacturer;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.exception.ConversionException;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.core.populator.AbstractDataPopulator;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;

/**
 * @author Carl Samson
 */
@Setter
@Getter
public class PersistableManufacturerPopulator
		extends AbstractDataPopulator<PersistableManufacturer, StoreMerchantId, Manufacturer> {

	@Override
	public Manufacturer populate(PersistableManufacturer source, Manufacturer target, StoreMerchantId store,
			LanguageCode language) throws ConversionException {

		try {

			target.setStoreMerchantId(store);
			target.setCode(source.getCode());

			if (!CollectionUtils.isEmpty(source.getDescriptions())) {
				Set<com.asrevo.cvhome.catalog.entity.product.manufacturer.ManufacturerDescription> descriptions = new HashSet<>();
				for (ManufacturerDescription description : source.getDescriptions()) {
					com.asrevo.cvhome.catalog.entity.product.manufacturer.ManufacturerDescription desc = new com.asrevo.cvhome.catalog.entity.product.manufacturer.ManufacturerDescription();
					if (desc.getId() != null && desc.getId() > 0) {
						desc.setId(description.getId());
					}
					if (target.getDescriptions() != null) {
						for (com.asrevo.cvhome.catalog.entity.product.manufacturer.ManufacturerDescription d : target
							.getDescriptions()) {
							if (d.getLanguageCode().equals(description.getLanguage())
									|| desc.getId() != null && d.getId().longValue() == desc.getId().longValue()) {
								desc = d;
							}
						}
					}

					desc.setManufacturer(target);
					desc.setDescription(description.getDescription());
					desc.setName(description.getName());
					desc.setLanguageCode(description.getLanguage());
					descriptions.add(desc);
				}
				target.setDescriptions(descriptions);
			}

		}
		catch (Exception e) {
			throw new ConversionException(e);
		}

		return target;
	}

	@Override
	protected Manufacturer createTarget() {
		// TODO Auto-generated method stub
		return null;
	}

}
