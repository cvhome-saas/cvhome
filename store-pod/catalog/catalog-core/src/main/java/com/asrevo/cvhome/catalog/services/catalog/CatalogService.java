package com.asrevo.cvhome.catalog.services.catalog;

import com.asrevo.cvhome.catalog.entity.catalog.Catalog;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;
import java.util.Optional;

public interface CatalogService extends SalesManagerEntityService<Long, Catalog> {

	/**
	 * Creates a new Catalog
	 * @return Catalog
	 */
	Catalog saveOrUpdate(Catalog catalog, StoreMerchantId store);

	Optional<Catalog> getByCode(String code, StoreMerchantId store);

	/**
	 * Delete a Catalog and related objects
	 */
	void delete(Catalog catalog);

}
