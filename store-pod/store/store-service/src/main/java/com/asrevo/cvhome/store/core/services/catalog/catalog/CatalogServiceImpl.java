package com.asrevo.cvhome.store.core.services.catalog.catalog;

import com.asrevo.cvhome.store.core.entity.catalog.catalog.Catalog;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.repositories.catalog.catalog.CatalogRepository;
import com.asrevo.cvhome.store.core.repositories.catalog.catalog.PageableCatalogRepository;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service("catalogService")
public class CatalogServiceImpl extends SalesManagerEntityServiceImpl<Long, Catalog>
        implements CatalogService {

    private final CatalogRepository catalogRepository;

    private final PageableCatalogRepository pageableCatalogRepository;

    @Autowired
    public CatalogServiceImpl(
            CatalogRepository repository, PageableCatalogRepository pageableCatalogRepository) {
        super(repository);
        this.catalogRepository = repository;
        this.pageableCatalogRepository = pageableCatalogRepository;
    }

    @Override
    public Catalog saveOrUpdate(Catalog catalog, MerchantStore store) {
        catalogRepository.save(catalog);
        return catalog;
    }

    @Override
    public Page<Catalog> getCatalogs(
            MerchantStore store, Language language, String name, int page, int count) {
        Pageable pageRequest = PageRequest.of(page, count);
        return pageableCatalogRepository.listByStore(store.getId(), name, pageRequest);
    }

    @Override
    public void delete(Catalog catalog) throws ServiceException {
        Assert.notNull(catalog, "Catalog must not be null");
        catalogRepository.delete(catalog);
    }

    @Override
    public Optional<Catalog> getById(Long catalogId, MerchantStore store) {
        return catalogRepository.findById(catalogId, store.getId());
    }

    @Override
    public Optional<Catalog> getByCode(String code, MerchantStore store) {
        return catalogRepository.findByCode(code, store.getId());
    }

    @Override
    public boolean existByCode(String code, MerchantStore store) {
        return catalogRepository.existsByCode(code, store.getId());
    }
}
