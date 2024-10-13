package com.asrevo.cvhome.store.core.services.catalog.catalog;

import com.asrevo.cvhome.store.core.entity.catalog.catalog.Catalog;
import com.asrevo.cvhome.store.core.entity.catalog.catalog.CatalogCategoryEntry;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;
import org.springframework.data.domain.Page;

public interface CatalogEntryService extends SalesManagerEntityService<Long, CatalogCategoryEntry> {

    void add(CatalogCategoryEntry entry, Catalog catalog);

    void remove(CatalogCategoryEntry catalogEntry) throws ServiceException;

    Page<CatalogCategoryEntry> list(
            Catalog catalog,
            MerchantStore store,
            Language language,
            String name,
            int page,
            int count);
}
