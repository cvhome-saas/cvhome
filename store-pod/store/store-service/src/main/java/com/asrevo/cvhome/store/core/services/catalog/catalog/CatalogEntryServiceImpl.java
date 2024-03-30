package com.asrevo.cvhome.store.core.services.catalog.catalog;

import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.repositories.catalog.catalog.CatalogEntryRepository;
import com.asrevo.cvhome.store.core.repositories.catalog.catalog.PageableCatalogEntryRepository;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;
import com.asrevo.cvhome.store.core.entity.catalog.catalog.Catalog;
import com.asrevo.cvhome.store.core.entity.catalog.catalog.CatalogCategoryEntry;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service("catalogEntryService")
public class CatalogEntryServiceImpl extends SalesManagerEntityServiceImpl<Long, CatalogCategoryEntry>
        implements CatalogEntryService {

    @Autowired
    private PageableCatalogEntryRepository pageableCatalogEntryRepository;

    private CatalogEntryRepository catalogEntryRepository;

    @Autowired
    public CatalogEntryServiceImpl(CatalogEntryRepository repository) {
        super(repository);
        this.catalogEntryRepository = repository;
    }

    @Override
    public void add(CatalogCategoryEntry entry, Catalog catalog) {
        entry.setCatalog(catalog);
        catalogEntryRepository.save(entry);
    }


    @Override
    public Page<CatalogCategoryEntry> list(Catalog catalog, MerchantStore store, Language language, String name, int page,
                                           int count) {
        Pageable pageRequest = PageRequest.of(page, count);
        return pageableCatalogEntryRepository.listByCatalog(catalog.getId(), store.getId(), language.getId(), name, pageRequest);

    }

    @Override
    public void remove(CatalogCategoryEntry catalogEntry) throws ServiceException {
        catalogEntryRepository.delete(catalogEntry);

    }


}
