package com.asrevo.cvhome.catalog.service.facade.product;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.entity.product.attribute.ProductAttribute;
import com.asrevo.cvhome.catalog.entity.product.attribute.ProductOption;
import com.asrevo.cvhome.catalog.entity.product.attribute.ProductOptionValue;
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
import com.asrevo.cvhome.store.core.model.entity.CodeEntity;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductOptionFacadeImpl implements ProductOptionFacade {

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
                                                  LanguageCode language)
            throws ProductOptionNotFoundException, ProductOptionNotConvertibleException {
        ProductOption optionModel = new ProductOption();
        if (option.getId() != null && option.getId() > 0) {
            optionModel = productOptionService.getById(store, option.getId());
            if (optionModel == null) {
                throw ProductOptionNotFoundException.of(option.getId(), store);
            }
        }

        optionModel = persistableeMapper.merge(option, optionModel, store, language);
        productOptionService.saveOrUpdate(optionModel);

        optionModel = productOptionService.getById(store, optionModel.getId());
        return readableMapper.convert(optionModel, store, language);
    }

    @Override
    public void deleteOption(Long optionId, StoreMerchantId store)
            throws ProductOptionNotFoundException {
        ProductOption optionModel = productOptionService.getById(store, optionId);
        if (optionModel == null) {
            throw ProductOptionNotFoundException.of(optionId, store);
        }
        productOptionService.delete(optionModel);
    }

    @Override
    public void deleteOptionValue(Long optionValueId, StoreMerchantId store)
            throws ProductOptionValueNotFoundException {
        ProductOptionValue optionModel = productOptionValueService.getById(store, optionValueId);
        if (optionModel == null) {
            throw ProductOptionValueNotFoundException.of(optionValueId, store);
        }
        productOptionValueService.delete(optionModel);
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
    public ReadableProductOptionEntity getOption(Long optionId, StoreMerchantId store, LanguageCode language)
            throws ProductOptionNotFoundException {
        ProductOption option = productOptionService.getById(store, optionId);

        if (option == null) {
            throw ProductOptionNotFoundException.of(optionId, store);
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
                                                      LanguageCode language)
            throws ProductOptionValueNotFoundException, ProductOptionNotConvertibleException {
        ProductOptionValue value = new ProductOptionValue();
        if (optionValue.getId() != null && optionValue.getId() > 0) {
            value = productOptionValueService.getById(store, optionValue.getId());
            if (value == null) {
                throw ProductOptionValueNotFoundException.of(optionValue.getId(), store);
            }
        }

        value = persistableOptionValueMapper.merge(optionValue, value, store, language);

        productOptionValueService.saveOrUpdate(value);

        ProductOptionValue optValue = productOptionValueService.getById(store, value.getId());

        // convert to readable
        ReadableProductOptionValue readableProductOptionValue = new ReadableProductOptionValue();
        readableProductOptionValue = readableOptionValueMapper.merge(optValue, readableProductOptionValue, store,
                language);

        return readableProductOptionValue;
    }

    @Override
    public ReadableProductOptionValue getOptionValue(Long optionValueId, StoreMerchantId store, LanguageCode language)
            throws ProductOptionValueNotFoundException {

        ProductOptionValue optionValue = productOptionValueService.getById(store, optionValueId);

        if (optionValue == null) {
            throw ProductOptionValueNotFoundException.of(optionValueId, store);
        }

        return readableOptionValueMapper.convert(optionValue, store, language);
    }

    @Override
    public ReadableProductAttributeEntity saveAttribute(Long productId, PersistableProductAttribute attribute,
                                                        StoreMerchantId store, LanguageCode language)

            throws ProductAttributeNotFoundException, ProductOptionReferenceUnresolvableException,
            ProductOptionValueReferenceUnresolvableException,
            ProductReferenceUnresolvableException, ProductAttributeNotConvertibleException,
            ProductOptionNotConvertibleException {
        attribute.setProductId(productId);
        ProductAttribute attr = new ProductAttribute();
        if (attribute.getId() != null && attribute.getId() > 0) {
            attr = productAttributeService.getById(attribute.getId());
            if (attr == null || productId != attr.getProduct().getId().longValue()) {
                throw ProductAttributeNotFoundException.of(attribute.getId(), store);
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
                                                       LanguageCode language)
            throws ProductAttributeNotFoundException, ProductAttributeNotConvertibleException {

        ProductAttribute attr = requireAttribute(productId, attributeId, store);
        return readableProductAttributeMapper.convert(attr, store, language);
    }

    /**
     * The three checks the old code repeated at every call site, with three near-identical messages: the attribute
     * exists, belongs to that product, and that product belongs to this store. All three now answer the same 404,
     * because to a caller they are one question.
     */
    private ProductAttribute requireAttribute(Long productId, Long attributeId, StoreMerchantId store)
            throws ProductAttributeNotFoundException {
        ProductAttribute attr = productAttributeService.getById(attributeId);
        if (attr == null || attr.getProduct().getId().longValue() != productId
                || !Objects.equals(attr.getProduct().getStore(), store)) {
            throw ProductAttributeNotFoundException.of(attributeId, store);
        }
        return attr;
    }

    private Product product(long id, StoreMerchantId store) throws ProductNotFoundException {
        Product product = productService.getById(id);

        if (product == null || !Objects.equals(product.getStore(), store)) {
            throw ProductNotFoundException.of(id, store);
        }

        return product;
    }

    @Override
    public ReadableProductAttributeList getAttributesList(Long productId, StoreMerchantId store, LanguageCode language,
                                                          Pageable pageable)
            throws ProductNotFoundException, ProductAttributeNotConvertibleException {

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

        // A plain loop rather than stream().map(...): the attribute mapper declares a checked failure now.
        List<ReadableProductAttributeEntity> values = new ArrayList<>();
        for (ProductAttribute attribute : attr.getContent()) {
            values.add(readableProductAttributeMapper.convert(attribute, store, language));
        }

        attrList.setContent(values);

        return attrList;
    }

    @Override
    public void deleteAttribute(Long productId, Long attributeId, StoreMerchantId store)
            throws ProductAttributeNotFoundException {
        productAttributeService.delete(requireAttribute(productId, attributeId, store));
    }


    @Override
    public List<CodeEntity> createAttributes(List<PersistableProductAttribute> attributes, Long productId,
                                             StoreMerchantId store)
            throws ProductNotFoundException, ProductOptionReferenceUnresolvableException, ProductOptionValueReferenceUnresolvableException,
            ProductReferenceUnresolvableException, ProductAttributeNotConvertibleException,
            ProductOptionNotConvertibleException {

        // A plain loop rather than stream().map(...): the attribute mapper declares checked failures now.
        List<ProductAttribute> modelAttributes = new ArrayList<>();
        for (PersistableProductAttribute attr : attributes) {
            modelAttributes.add(persistableProductAttributeMapper.convert(attr, store, null));
        }

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
