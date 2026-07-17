package com.asrevo.cvhome.catalog.service.facade.manufacturer;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.catalog.entity.product.manufacturer.Manufacturer;
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

    List<ReadableManufacturer> getByProductInCategory(StoreMerchantId store, LanguageCode language, Long categoryId);

    /**
     * Creates or saves a manufacturer
     */
    void saveOrUpdateManufacturer(PersistableManufacturer manufacturer, StoreMerchantId store, LanguageCode language)
            throws Exception;

    /**
     * Deletes a manufacturer
     */
    void deleteManufacturer(Manufacturer manufacturer) throws Exception;

    /**
     * Get a Manufacturer by id
     */
    ReadableManufacturer getManufacturer(Long id, StoreMerchantId store, LanguageCode language);

    /**
     * List manufacturers by a specific store
     */
    ReadableManufacturerList listByStore(StoreMerchantId store, LanguageCode language, ListCriteria criteria,
                                         Pageable pageable);

    /**
     * Determines if manufacturer code already exists
     */
    boolean manufacturerExist(StoreMerchantId store, String manufacturerCode);

}
