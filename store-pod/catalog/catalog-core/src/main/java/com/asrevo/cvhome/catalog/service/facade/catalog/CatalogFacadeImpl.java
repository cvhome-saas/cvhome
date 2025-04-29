package com.asrevo.cvhome.catalog.service.facade.catalog;

import com.asrevo.cvhome.catalog.entity.catalog.Catalog;
import com.asrevo.cvhome.catalog.services.catalog.CatalogService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import org.hibernate.service.spi.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service("catalogFacade")
public class CatalogFacadeImpl implements CatalogFacade {

    private final CatalogService catalogService;

    public CatalogFacadeImpl(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @Override
    public Catalog getCatalog(String code, StoreMerchantId store) {
        Assert.notNull(code, "Catalog code cannot be null");
        Assert.notNull(store, "store cannot be null");

        return catalogService
                .getByCode(code, store)
                .orElseThrow(() -> new ServiceException("Catalog not found"));
    }
}
