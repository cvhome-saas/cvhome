package com.asrevo.cvhome.catalog.services.catalog;

import com.asrevo.cvhome.catalog.entity.catalog.Catalog;
import com.asrevo.cvhome.catalog.repositories.catalog.CatalogRepository;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service("catalogService")
public class CatalogServiceImpl extends SalesManagerEntityServiceImpl<Long, Catalog>
        implements CatalogService {

    private final CatalogRepository catalogRepository;

    @Autowired
    public CatalogServiceImpl(CatalogRepository repository) {
        super(repository);
        this.catalogRepository = repository;
    }

    @Override
    public Catalog saveOrUpdate(Catalog catalog, StoreMerchantId store) {
        catalog.setStoreMerchantId(store);
        catalogRepository.save(catalog);
        return catalog;
    }

    @Override
    public void delete(Catalog catalog) {
        Assert.notNull(catalog, "Catalog must not be null");
        catalogRepository.delete(catalog);
    }

    @Override
    public Optional<Catalog> getByCode(String code, StoreMerchantId storeMerchantId) {
        return catalogRepository.findByCode(code, storeMerchantId);
    }
}
