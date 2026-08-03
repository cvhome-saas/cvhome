package com.asrevo.cvhome.catalog.service.facade.product;

import com.asrevo.cvhome.catalog.errors.CategoryReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.errors.InventoryNotConvertibleException;
import com.asrevo.cvhome.catalog.errors.ManufacturerReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.errors.ProductAttributeNotConvertibleException;
import com.asrevo.cvhome.catalog.errors.ProductNotConvertibleException;
import com.asrevo.cvhome.catalog.errors.ProductNotFoundException;
import com.asrevo.cvhome.catalog.errors.ProductNotPersistedException;
import com.asrevo.cvhome.catalog.errors.ProductOptionNotConvertibleException;
import com.asrevo.cvhome.catalog.errors.ProductOptionReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.errors.ProductOptionValueReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.errors.ProductReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.errors.ProductTypeReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.model.product.product.definition.PersistableProductDefinition;
import com.asrevo.cvhome.catalog.model.product.product.definition.ReadableProductDefinition;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.exception.ServiceException;

public interface ProductDefinitionFacade {

    /**
     *
     */
    Long saveProductDefinition(StoreMerchantId store, PersistableProductDefinition product, LanguageCode language)
            throws ProductNotConvertibleException, ManufacturerReferenceUnresolvableException,
            ProductTypeReferenceUnresolvableException, CategoryReferenceUnresolvableException,
            ProductOptionReferenceUnresolvableException, ProductOptionValueReferenceUnresolvableException,
            ProductReferenceUnresolvableException, ProductAttributeNotConvertibleException,
            ProductOptionNotConvertibleException, ProductNotPersistedException,
            ProductNotFoundException, ServiceException;

    /**
     *
     */
    void update(Long productId, PersistableProductDefinition product, StoreMerchantId merchant, LanguageCode language)
            throws ProductNotConvertibleException, ManufacturerReferenceUnresolvableException,
            ProductTypeReferenceUnresolvableException, CategoryReferenceUnresolvableException,
            ProductOptionReferenceUnresolvableException, ProductOptionValueReferenceUnresolvableException,
            ProductReferenceUnresolvableException, ProductAttributeNotConvertibleException,
            ProductOptionNotConvertibleException, ProductNotPersistedException,
            ProductNotFoundException, ServiceException;

    /**
     *
     */
    ReadableProductDefinition getProduct(StoreMerchantId store, Long id, LanguageCode language)
            throws InventoryNotConvertibleException;

}
