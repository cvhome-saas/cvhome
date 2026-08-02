package com.asrevo.cvhome.catalog.service.facade.product;

import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.entity.product.attribute.ProductAttribute;
import com.asrevo.cvhome.catalog.entity.product.attribute.ProductOption;
import com.asrevo.cvhome.catalog.entity.product.attribute.ProductOptionValue;
import com.asrevo.cvhome.catalog.model.product.attribute.PersistableProductAttribute;
import com.asrevo.cvhome.catalog.model.product.attribute.PersistableProductOptionValue;
import com.asrevo.cvhome.catalog.model.product.attribute.api.PersistableProductOptionEntity;
import com.asrevo.cvhome.catalog.model.product.attribute.api.ReadableProductAttributeEntity;
import com.asrevo.cvhome.catalog.model.product.attribute.api.ReadableProductAttributeList;
import com.asrevo.cvhome.catalog.model.product.attribute.api.ReadableProductOptionEntity;
import com.asrevo.cvhome.catalog.model.product.attribute.api.ReadableProductOptionList;
import com.asrevo.cvhome.catalog.model.product.attribute.api.ReadableProductOptionValue;
import com.asrevo.cvhome.catalog.model.product.attribute.api.ReadableProductOptionValueList;
import com.asrevo.cvhome.catalog.service.mapper.catalog.PersistableProductAttributeMapper;
import com.asrevo.cvhome.catalog.service.mapper.catalog.PersistableProductOptionMapper;
import com.asrevo.cvhome.catalog.service.mapper.catalog.PersistableProductOptionValueMapper;
import com.asrevo.cvhome.catalog.service.mapper.catalog.ReadableProductAttributeMapper;
import com.asrevo.cvhome.catalog.service.mapper.catalog.ReadableProductOptionMapper;
import com.asrevo.cvhome.catalog.service.mapper.catalog.ReadableProductOptionValueMapper;
import com.asrevo.cvhome.catalog.services.product.ProductService;
import com.asrevo.cvhome.catalog.services.product.attribute.ProductAttributeService;
import com.asrevo.cvhome.catalog.services.product.attribute.ProductOptionService;
import com.asrevo.cvhome.catalog.services.product.attribute.ProductOptionValueService;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.controller.exception.ResourceNotFoundException;
import com.asrevo.cvhome.store.controller.exception.ServiceRuntimeException;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.entity.CodeEntity;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductOptionFacadeImpl implements ProductOptionFacade {

    private static final String PRODUCT_OPTION_NOT_FOUND_FOR_IF_TEMPLATE = "ProductOption not found for if [%s] and store [%s]";

    private static final String PRODUCT_OPTION_NOT_FOUND_TEMPLATE = "ProductOption not found for [%s] and store [%s]";

    private static final String DELETING_PRODUCT_OPTION_ERROR_TEMPLATE = "An exception occured while deleting ProductOption [%s]";

    private static final String PRODUCT_OPTION_VALUE_NOT_FOUND_TEMPLATE = "ProductOptionValue not found for  [%s] and store [%s]";

    private static final String DELETING_PRODUCT_OPTION_VALUE_ERROR_TEMPLATE =
            "An exception occured while deleting ProductOptionValue [%s]";

    private static final String OPTION_ID_NOT_FOUND_TEMPLATE = "Option id [%s] not found";

    private static final String PRODUCT_OPTION_VALUE_DOES_NOT_EXIST_TEMPLATE =
            "ProductOptionValue [%s] does not exists for store [%s]";

    private static final String OPTION_VALUE_ID_NOT_FOUND_TEMPLATE = "OptionValue id [%s] not found";

    private static final String PRODUCT_ATTRIBUTE_NOT_FOUND_TEMPLATE = "Product attribute [%s] not found";

    private static final String PRODUCT_ATTRIBUTE_NOT_FOUND_FOR_PRODUCT_TEMPLATE =
            "Product attribute [%s] not found for product [%s]";

    private static final String ATTRIBUTE_NOT_FOUND_FOR_STORE_TEMPLATE = "ProductAttribute not found for [%s] and store [%s]";

    private static final String ATTRIBUTE_NOT_FOUND_FOR_PRODUCT_TEMPLATE =
            "ProductAttribute not found for [%s] and product [%s]";

    private static final String ATTRIBUTE_NOT_FOUND_FOR_PRODUCT_AND_STORE_TEMPLATE =
            "ProductAttribute not found for [%s] and product [%s] and store [%s]";

    private static final String PRODUCT_NOT_FOUND_FOR_ID_TEMPLATE = "Productnot found for id [%s]";

    private static final String PRODUCT_NOT_FOUND_ID_FOR_STORE_TEMPLATE = "Productnot found id [%s] for store [%s]";

    private static final String DELETING_PRODUCT_ATTRIBUTE_ERROR_TEMPLATE =
            "An exception occured while deleting ProductAttribute [%s]";

    private final ProductOptionService productOptionService;

    private final ProductOptionValueService productOptionValueService;

    private final ReadableProductOptionMapper readableMapper;

    private final PersistableProductOptionMapper persistableeMapper;

    private final PersistableProductOptionValueMapper persistableOptionValueMapper;

    private final ReadableProductOptionValueMapper readableOptionValueMapper;

    private final ProductAttributeService productAttributeService;

    private final PersistableProductAttributeMapper persistableProductAttributeMapper;

    private final ReadableProductAttributeMapper readableProductAttributeMapper;

    private final ProductService productService;

    @Override
    public ReadableProductOptionEntity saveOption(PersistableProductOptionEntity option, StoreMerchantId store,
                                                  LanguageCode language) {
        ProductOption optionModel = new ProductOption();
        if (option.getId() != null && option.getId() > 0) {
            optionModel = productOptionService.getById(store, option.getId());
            if (optionModel == null) {
                throw new ResourceNotFoundException(
                        PRODUCT_OPTION_NOT_FOUND_FOR_IF_TEMPLATE.formatted(option.getId(), store));
            }
        }

        optionModel = persistableeMapper.merge(option, optionModel, store, language);
        try {
            productOptionService.saveOrUpdate(optionModel);
        } catch (ServiceException e) {
            throw new ServiceRuntimeException("An exception occured while saving ProductOption", e);
        }

        optionModel = productOptionService.getById(store, optionModel.getId());
        return readableMapper.convert(optionModel, store, language);
    }

    @Override
    public void deleteOption(Long optionId, StoreMerchantId store) {
        ProductOption optionModel = productOptionService.getById(store, optionId);
        if (optionModel == null) {
            throw new ResourceNotFoundException(PRODUCT_OPTION_NOT_FOUND_TEMPLATE.formatted(optionId, store));
        }
        try {
            productOptionService.delete(optionModel);
        } catch (ServiceException e) {
            throw new ServiceRuntimeException(DELETING_PRODUCT_OPTION_ERROR_TEMPLATE.formatted(optionId), e);
        }
    }

    @Override
    public void deleteOptionValue(Long optionValueId, StoreMerchantId store) {
        ProductOptionValue optionModel = productOptionValueService.getById(store, optionValueId);
        if (optionModel == null) {
            throw new ResourceNotFoundException(PRODUCT_OPTION_VALUE_NOT_FOUND_TEMPLATE.formatted(optionValueId, store));
        }
        try {
            productOptionValueService.delete(optionModel);
        } catch (ServiceException e) {
            throw new ServiceRuntimeException(DELETING_PRODUCT_OPTION_VALUE_ERROR_TEMPLATE.formatted(optionValueId), e);
        }
    }

    @Override
    public ReadableProductOptionValueList optionValues(StoreMerchantId store, LanguageCode language, String name,
                                                       Pageable pageable) {
        Page<ProductOptionValue> options = productOptionValueService.getByMerchant(store, null, name, pageable);
        ReadableProductOptionValueList valueList = new ReadableProductOptionValueList();
        valueList.setTotalPages(options.getTotalPages());
        valueList.setTotalElements(options.getTotalElements());
        valueList.setSize(options.getSize());
        valueList.setPageNumber(options.getNumber());

        List<ReadableProductOptionValue> values = options.getContent()
                .stream()
                .map(option -> readableOptionValueMapper.convert(option, store, null))
                .toList();

        valueList.setContent(values);

        return valueList;
    }

    @Override
    public ReadableProductOptionList options(StoreMerchantId store, LanguageCode language, String name,
                                             Pageable pageable) {
        Page<ProductOption> options = productOptionService.getByMerchant(store, null, name, pageable);
        ReadableProductOptionList valueList = new ReadableProductOptionList();
        valueList.setTotalPages(options.getTotalPages());
        valueList.setTotalElements(options.getTotalElements());
        valueList.setSize(options.getSize());
        valueList.setPageNumber(options.getNumber());

        List<ReadableProductOptionEntity> values = options.getContent()
                .stream()
                .map(option -> readableMapper.convert(option, store, null))
                .toList();

        valueList.setContent(values);

        return valueList;
    }

    @Override
    public ReadableProductOptionEntity getOption(Long optionId, StoreMerchantId store, LanguageCode language) {
        ProductOption option = productOptionService.getById(store, optionId);

        if (option == null) {
            throw new ResourceNotFoundException(OPTION_ID_NOT_FOUND_TEMPLATE.formatted(optionId));
        }

        return readableMapper.convert(option, store, language);
    }

    @Override
    public boolean optionExists(String code, StoreMerchantId store) {
        boolean exists = false;
        ProductOption option = productOptionService.getByCode(store, code);
        if (option != null) {
            exists = true;
        }
        return exists;
    }

    @Override
    public boolean optionValueExists(String code, StoreMerchantId store) {
        boolean exists = false;
        ProductOptionValue optionValue = productOptionValueService.getByCode(store, code);
        if (optionValue != null) {
            exists = true;
        }
        return exists;
    }

    @Override
    public ReadableProductOptionValue saveOptionValue(PersistableProductOptionValue optionValue, StoreMerchantId store,
                                                      LanguageCode language) {
        ProductOptionValue value = new ProductOptionValue();
        if (optionValue.getId() != null && optionValue.getId() > 0) {
            value = productOptionValueService.getById(store, optionValue.getId());
            if (value == null) {
                throw new ResourceNotFoundException(
                        PRODUCT_OPTION_VALUE_DOES_NOT_EXIST_TEMPLATE.formatted(optionValue.getId(), store));
            }
        }

        value = persistableOptionValueMapper.merge(optionValue, value, store, language);

        try {
            productOptionValueService.saveOrUpdate(value);
        } catch (ServiceException e) {
            throw new ServiceRuntimeException("Exception while saving option value", e);
        }

        ProductOptionValue optValue = productOptionValueService.getById(store, value.getId());

        // convert to readable
        ReadableProductOptionValue readableProductOptionValue = new ReadableProductOptionValue();
        readableProductOptionValue = readableOptionValueMapper.merge(optValue, readableProductOptionValue, store,
                language);

        return readableProductOptionValue;
    }

    @Override
    public ReadableProductOptionValue getOptionValue(Long optionValueId, StoreMerchantId store, LanguageCode language) {

        ProductOptionValue optionValue = productOptionValueService.getById(store, optionValueId);

        if (optionValue == null) {
            throw new ResourceNotFoundException(OPTION_VALUE_ID_NOT_FOUND_TEMPLATE.formatted(optionValueId));
        }

        return readableOptionValueMapper.convert(optionValue, store, language);
    }

    @Override
    public ReadableProductAttributeEntity saveAttribute(Long productId, PersistableProductAttribute attribute,
                                                        StoreMerchantId store, LanguageCode language) {
        attribute.setProductId(productId);
        ProductAttribute attr = new ProductAttribute();
        if (attribute.getId() != null && attribute.getId() > 0) {
            attr = productAttributeService.getById(attribute.getId());
            if (attr == null) {
                throw new ResourceNotFoundException(PRODUCT_ATTRIBUTE_NOT_FOUND_TEMPLATE.formatted(attribute.getId()));
            }

            if (productId != attr.getProduct().getId().longValue()) {
                throw new ResourceNotFoundException(
                        PRODUCT_ATTRIBUTE_NOT_FOUND_FOR_PRODUCT_TEMPLATE.formatted(attribute.getId(), productId));
            }
        }

        attr = persistableProductAttributeMapper.merge(attribute, attr, store, language);

        productAttributeService.saveOrUpdate(attr);

        // refresh
        attr = productAttributeService.getById(attr.getId());

        return readableProductAttributeMapper.convert(attr, store, language);
    }

    @Override
    public ReadableProductAttributeEntity getAttribute(Long productId, Long attributeId, StoreMerchantId store,
                                                       LanguageCode language) {

        ProductAttribute attr = productAttributeService.getById(attributeId);

        if (attr == null) {
            throw new ResourceNotFoundException(ATTRIBUTE_NOT_FOUND_FOR_STORE_TEMPLATE.formatted(attributeId, store));
        }

        if (attr.getProduct().getId().longValue() != productId) {
            throw new ResourceNotFoundException(
                    ATTRIBUTE_NOT_FOUND_FOR_PRODUCT_TEMPLATE.formatted(attributeId, productId));
        }

        if (!Objects.equals(attr.getProduct().getStore(), store)) {
            throw new ResourceNotFoundException(
                    ATTRIBUTE_NOT_FOUND_FOR_PRODUCT_AND_STORE_TEMPLATE.formatted(attributeId, productId, store));
        }

        return readableProductAttributeMapper.convert(attr, store, language);
    }

    private Product product(long id, StoreMerchantId store) {
        Product product = productService.getById(id);

        if (product == null) {
            throw new ResourceNotFoundException(PRODUCT_NOT_FOUND_FOR_ID_TEMPLATE.formatted(id));
        }

        if (!Objects.equals(product.getStore(), store)) {
            throw new ResourceNotFoundException(PRODUCT_NOT_FOUND_ID_FOR_STORE_TEMPLATE.formatted(id, store));
        }

        return product;
    }

    @Override
    public ReadableProductAttributeList getAttributesList(Long productId, StoreMerchantId store, LanguageCode language,
                                                          Pageable pageable) {

        Product product = this.product(productId, store);

        ReadableProductAttributeList attrList = new ReadableProductAttributeList();
        Page<ProductAttribute> attr;

        if (language != null) {
            attr = productAttributeService.getByProductId(store, product, language, pageable);
        } else {
            attr = productAttributeService.getByProductId(store, product, pageable);
        }
        attrList.setTotalElements(attr.getTotalElements());
        attrList.setSize(attr.getSize());
        attrList.setTotalPages(attr.getTotalPages());
        attrList.setPageNumber(attr.getNumber());

        List<ReadableProductAttributeEntity> values = attr.getContent()
                .stream()
                .map(attribute -> readableProductAttributeMapper.convert(attribute, store, language))
                .toList();

        attrList.setContent(values);

        return attrList;
    }

    @Override
    public void deleteAttribute(Long productId, Long attributeId, StoreMerchantId store) {
        try {

            ProductAttribute attr = productAttributeService.getById(attributeId);
            if (attr == null) {
                throw new ResourceNotFoundException(
                        ATTRIBUTE_NOT_FOUND_FOR_STORE_TEMPLATE.formatted(attributeId, store));
            }

            if (attr.getProduct().getId().longValue() != productId) {
                throw new ResourceNotFoundException(
                        ATTRIBUTE_NOT_FOUND_FOR_PRODUCT_TEMPLATE.formatted(attributeId, productId));
            }

            if (!Objects.equals(attr.getProduct().getStore(), store)) {
                throw new ResourceNotFoundException(
                        ATTRIBUTE_NOT_FOUND_FOR_PRODUCT_AND_STORE_TEMPLATE.formatted(attributeId, productId, store));
            }

            productAttributeService.delete(attr);

        } catch (ServiceException e) {
            throw new ServiceRuntimeException(DELETING_PRODUCT_ATTRIBUTE_ERROR_TEMPLATE.formatted(attributeId), e);
        }
    }


    @Override
    public List<CodeEntity> createAttributes(List<PersistableProductAttribute> attributes, Long productId,
                                             StoreMerchantId store) {

        List<ProductAttribute> modelAttributes = attributes.stream()
                .map(attr -> persistableProductAttributeMapper.convert(attr, store, null))
                .toList();

        productAttributeService.saveAll(modelAttributes);

        // save to a product
        Product product = this.product(productId, store);
        product.getAttributes().addAll(modelAttributes);

        productService.save(product);

        return modelAttributes.stream().map(this::codeEntity).toList();
    }

    private CodeEntity codeEntity(ProductAttribute attr) {
        CodeEntity entity = new CodeEntity();
        entity.setId(attr.getId());
        entity.setCode(attr.getProductOption().getCode());
        return entity;
    }

}
