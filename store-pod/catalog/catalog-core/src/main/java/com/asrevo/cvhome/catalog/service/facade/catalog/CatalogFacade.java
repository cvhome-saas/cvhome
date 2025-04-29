package com.asrevo.cvhome.catalog.service.facade.catalog;

import com.asrevo.cvhome.catalog.entity.catalog.Catalog;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

public interface CatalogFacade {

    Catalog getCatalog(String code, StoreMerchantId store);
}
