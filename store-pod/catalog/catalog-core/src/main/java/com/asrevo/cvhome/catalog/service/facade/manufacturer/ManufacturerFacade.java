package com.asrevo.cvhome.catalog.service.facade.manufacturer;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.catalog.entity.product.manufacturer.Manufacturer;
import com.asrevo.cvhome.catalog.errors.CategoryNotFoundException;
import com.asrevo.cvhome.catalog.errors.ForeignStoreProductAccessException;
import com.asrevo.cvhome.catalog.errors.ManufacturerNotConvertibleException;
import com.asrevo.cvhome.catalog.errors.ManufacturerNotFoundException;
import com.asrevo.cvhome.catalog.model.manufacturer.PersistableManufacturer;
import com.asrevo.cvhome.catalog.model.manufacturer.ReadableManufacturer;
import com.asrevo.cvhome.catalog.model.manufacturer.ReadableManufacturerList;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.model.entity.ListCriteria;

/**
 * Manufacturer / brand / collection product grouping
 *
 * @author carlsamson
 */
public interface ManufacturerFacade {

    /**
     * @throws CategoryNotFoundException          no such category in this store
     * @throws ForeignStoreProductAccessException the category belongs to another store
     */
    List<ReadableManufacturer> getByProductInCategory(StoreMerchantId store, LanguageCode language, Long categoryId)
            throws CategoryNotFoundException, ForeignStoreProductAccessException, ManufacturerNotConvertibleException;

    /**
     * Creates or saves a manufacturer.
     *
     * @throws ManufacturerNotFoundException the id given matches no manufacturer in this store
     */
    void saveOrUpdateManufacturer(PersistableManufacturer manufacturer, StoreMerchantId store, LanguageCode language)
            throws ManufacturerNotFoundException, ManufacturerNotConvertibleException;

    /**
     * Deletes a manufacturer.
     *
     */
    void deleteManufacturer(Manufacturer manufacturer);

    /**
     * Get a Manufacturer by id.
     *
     * @throws ManufacturerNotFoundException no such manufacturer in this store
     */
    ReadableManufacturer getManufacturer(Long id, StoreMerchantId store, LanguageCode language)
            throws ManufacturerNotFoundException, ManufacturerNotConvertibleException;

    /**
     * List manufacturers by a specific store
     */
    ReadableManufacturerList listByStore(StoreMerchantId store, LanguageCode language, ListCriteria criteria,
                                         Pageable pageable) throws ManufacturerNotConvertibleException;

    /**
     * Determines if manufacturer code already exists
     */
    boolean manufacturerExist(StoreMerchantId store, String manufacturerCode);

}
