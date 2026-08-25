package com.asrevo.cvhome.catalog.service.facade.product;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.catalog.errors.ProductAttributeNotConvertibleException;
import com.asrevo.cvhome.catalog.errors.ProductAttributeNotFoundException;
import com.asrevo.cvhome.catalog.errors.ProductNotFoundException;
import com.asrevo.cvhome.catalog.errors.ProductOptionNotConvertibleException;
import com.asrevo.cvhome.catalog.errors.ProductOptionNotFoundException;
import com.asrevo.cvhome.catalog.errors.ProductOptionReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.errors.ProductOptionValueNotFoundException;
import com.asrevo.cvhome.catalog.errors.ProductOptionValueReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.errors.ProductReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.model.product.attribute.PersistableProductAttribute;
import com.asrevo.cvhome.catalog.model.product.attribute.PersistableProductOptionValue;
import com.asrevo.cvhome.catalog.model.product.attribute.api.PersistableProductOptionEntity;
import com.asrevo.cvhome.catalog.model.product.attribute.api.ReadableProductAttributeEntity;
import com.asrevo.cvhome.catalog.model.product.attribute.api.ReadableProductAttributeList;
import com.asrevo.cvhome.catalog.model.product.attribute.api.ReadableProductOptionEntity;
import com.asrevo.cvhome.catalog.model.product.attribute.api.ReadableProductOptionList;
import com.asrevo.cvhome.catalog.model.product.attribute.api.ReadableProductOptionValue;
import com.asrevo.cvhome.catalog.model.product.attribute.api.ReadableProductOptionValueList;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.model.entity.CodeEntity;

/*
 * Attributes, Options and Options values management independently from Product
 */
public interface ProductOptionFacade {

    ReadableProductOptionEntity getOption(Long optionId, StoreMerchantId store, LanguageCode language)
            throws ProductOptionNotFoundException;

    ReadableProductOptionValue getOptionValue(Long optionValueId, StoreMerchantId store, LanguageCode language)
            throws ProductOptionValueNotFoundException;

    ReadableProductOptionEntity saveOption(PersistableProductOptionEntity option, StoreMerchantId store,
                                           LanguageCode language)
            throws ProductOptionNotFoundException, ProductOptionNotConvertibleException;

    ReadableProductOptionValue saveOptionValue(PersistableProductOptionValue optionValue, StoreMerchantId store,
                                               LanguageCode language)
            throws ProductOptionValueNotFoundException, ProductOptionNotConvertibleException;

    List<CodeEntity> createAttributes(List<PersistableProductAttribute> attributes, Long productId,
                                      StoreMerchantId store)
            throws ProductNotFoundException, ProductOptionReferenceUnresolvableException, ProductOptionValueReferenceUnresolvableException,
            ProductReferenceUnresolvableException, ProductAttributeNotConvertibleException,
            ProductOptionNotConvertibleException;

    boolean optionExists(String code, StoreMerchantId store);

    boolean optionValueExists(String code, StoreMerchantId store);

    void deleteOption(Long optionId, StoreMerchantId store)
            throws ProductOptionNotFoundException;

    void deleteOptionValue(Long optionValueId, StoreMerchantId store)
            throws ProductOptionValueNotFoundException;

    ReadableProductOptionList options(StoreMerchantId store, LanguageCode language, String name, Pageable pageable);

    ReadableProductOptionValueList optionValues(StoreMerchantId store, LanguageCode language, String name,
                                                Pageable pageable);

    ReadableProductAttributeEntity saveAttribute(Long productId, PersistableProductAttribute attribute,
                                                 StoreMerchantId store, LanguageCode language)

            throws ProductAttributeNotFoundException, ProductOptionReferenceUnresolvableException,
            ProductOptionValueReferenceUnresolvableException,
            ProductReferenceUnresolvableException, ProductAttributeNotConvertibleException,
            ProductOptionNotConvertibleException;

    ReadableProductAttributeEntity getAttribute(Long productId, Long attributeId, StoreMerchantId store,
                                                LanguageCode language)
            throws ProductAttributeNotFoundException, ProductAttributeNotConvertibleException;

    ReadableProductAttributeList getAttributesList(Long productId, StoreMerchantId store, LanguageCode language,
                                                   Pageable pageable)
            throws ProductNotFoundException, ProductAttributeNotConvertibleException;

    void deleteAttribute(Long productId, Long attributeId, StoreMerchantId store)
            throws ProductAttributeNotFoundException;

}
