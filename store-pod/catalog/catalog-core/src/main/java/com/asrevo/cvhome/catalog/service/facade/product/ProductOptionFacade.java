package com.asrevo.cvhome.catalog.service.facade.product;

import com.asrevo.cvhome.catalog.model.product.attribute.PersistableProductAttribute;
import com.asrevo.cvhome.catalog.model.product.attribute.PersistableProductOptionValue;
import com.asrevo.cvhome.catalog.model.product.attribute.api.*;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.model.entity.CodeEntity;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import org.springframework.data.domain.Pageable;

import java.util.List;

/*
 * Attributes, Options and Options values management independently from Product
 */
public interface ProductOptionFacade {

    ReadableProductOptionEntity getOption(
            Long optionId, StoreMerchantId store, LanguageCode language);

    ReadableProductOptionValue getOptionValue(
            Long optionValueId, StoreMerchantId store, LanguageCode language);

    ReadableProductOptionEntity saveOption(
            PersistableProductOptionEntity option, StoreMerchantId store, LanguageCode language);

    ReadableProductOptionValue saveOptionValue(
            PersistableProductOptionValue optionValue,
            StoreMerchantId store,
            LanguageCode language);

    List<CodeEntity> createAttributes(
            List<PersistableProductAttribute> attributes, Long productId, StoreMerchantId store);

    /*
        void addOptionValueImage(MultipartFile image, Long optionValueId, StoreMerchantId store, LanguageCode language);

        void removeOptionValueImage(Long optionValueId, StoreMerchantId store, LanguageCode language);
    */
    boolean optionExists(String code, StoreMerchantId store);

    boolean optionValueExists(String code, StoreMerchantId store);

    void deleteOption(Long optionId, StoreMerchantId store);

    void deleteOptionValue(Long optionValueId, StoreMerchantId store);

    ReadableProductOptionList options(
            StoreMerchantId store, LanguageCode language, String name, Pageable pageable);

    ReadableProductOptionValueList optionValues(
            StoreMerchantId store, LanguageCode language, String name, Pageable pageable);

    ReadableProductAttributeEntity saveAttribute(
            Long productId,
            PersistableProductAttribute attribute,
            StoreMerchantId store,
            LanguageCode language);

    ReadableProductAttributeEntity getAttribute(
            Long productId, Long attributeId, StoreMerchantId store, LanguageCode language);

    ReadableProductAttributeList getAttributesList(
            Long productId, StoreMerchantId store, LanguageCode language, Pageable pageable);

    void deleteAttribute(Long productId, Long attributeId, StoreMerchantId store);
}
