package com.asrevo.cvhome.store.service.facade.manufacturer;

import com.asrevo.cvhome.store.core.entity.catalog.product.manufacturer.Manufacturer;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.catalog.manufacturer.PersistableManufacturer;
import com.asrevo.cvhome.store.core.model.catalog.manufacturer.ReadableManufacturer;
import com.asrevo.cvhome.store.core.model.catalog.manufacturer.ReadableManufacturerList;
import com.asrevo.cvhome.store.core.model.entity.ListCriteria;

import java.util.List;

/**
 * Manufacturer / brand / collection product grouping
 *
 * @author carlsamson
 */
public interface ManufacturerFacade {

    List<ReadableManufacturer> getByProductInCategory(MerchantStore store, Language language, Long categoryId);

    /**
     * Creates or saves a manufacturer
     *
     */
    void saveOrUpdateManufacturer(PersistableManufacturer manufacturer, MerchantStore store,
                                  Language language) throws Exception;

    /**
     * Deletes a manufacturer
     *
     */
    void deleteManufacturer(Manufacturer manufacturer, MerchantStore store, Language language)
            throws Exception;

    /**
     * Get a Manufacturer by id
     *
     */
    ReadableManufacturer getManufacturer(Long id, MerchantStore store, Language language)
            throws Exception;

    /**
     * Get all Manufacturer
     *
     */
    ReadableManufacturerList getAllManufacturers(MerchantStore store, Language language, ListCriteria criteria, int page, int count);

    /**
     * List manufacturers by a specific store
     *
     */
    ReadableManufacturerList listByStore(MerchantStore store, Language language, ListCriteria criteria, int page, int count);

    /**
     * Determines if manufacturer code already exists
     *
     */
    boolean manufacturerExist(MerchantStore store, String manufacturerCode);

}
