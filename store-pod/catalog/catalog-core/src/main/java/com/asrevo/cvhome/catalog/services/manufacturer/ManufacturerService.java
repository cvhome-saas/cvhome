package com.asrevo.cvhome.catalog.services.manufacturer;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.catalog.errors.CategoryNotFoundException;
import com.asrevo.cvhome.catalog.errors.ManufacturerNotFoundException;
import com.asrevo.cvhome.catalog.model.manufacturer.PersistableManufacturer;
import com.asrevo.cvhome.catalog.model.manufacturer.ReadableManufacturer;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.model.entity.ReadableEntityList;

/**
 * Brands.
 */
public interface ManufacturerService {

    ReadableEntityList<ReadableManufacturer> list(StoreMerchantId store, String name, LanguageCode language,
                                                  Pageable pageable);

    ReadableManufacturer get(StoreMerchantId store, Long id, LanguageCode language)
            throws ManufacturerNotFoundException;

    /**
     * The brands of the products in a category and its subtree — the storefront's filter facet.
     */
    List<ReadableManufacturer> listByCategory(StoreMerchantId store, Long categoryId, LanguageCode language)
            throws CategoryNotFoundException;

    boolean exists(StoreMerchantId store, String code);

    /**
     * Creates the brand, or updates it when {@code id} is set; answers the id.
     */
    Long save(StoreMerchantId store, PersistableManufacturer manufacturer) throws ManufacturerNotFoundException;

    void delete(StoreMerchantId store, Long id) throws ManufacturerNotFoundException;
}
