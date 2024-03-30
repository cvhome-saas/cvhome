package com.asrevo.cvhome.store.core.services.catalog.catalog;

import com.asrevo.cvhome.store.core.entity.catalog.catalog.Catalog;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;
import org.springframework.data.domain.Page;

import java.util.Optional;

public interface CatalogService extends SalesManagerEntityService<Long, Catalog> {


    /**
     * Creates a new Catalog
     *
     * @param store
     * @return Catalog
     * @throws ServiceException
     */
    Catalog saveOrUpdate(Catalog catalog, MerchantStore store);

    Optional<Catalog> getById(Long catalogId, MerchantStore store);

    Optional<Catalog> getByCode(String code, MerchantStore store);

    /**
     * Get a list of Catalog associated with a MarketPlace
     *
     * @param marketPlace
     * @return List<Catalog>
     * @throws ServiceException
     */
    Page<Catalog> getCatalogs(MerchantStore store, Language language, String name, int page, int count);

    /**
     * Delete a Catalog and related objects
     */
    void delete(Catalog catalog) throws ServiceException;

    boolean existByCode(String code, MerchantStore store);

}
