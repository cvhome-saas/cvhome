package com.asrevo.cvhome.catalog.service.facade.product;

import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

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
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.controller.exception.ResourceNotFoundException;
import com.asrevo.cvhome.store.controller.exception.ServiceRuntimeException;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.entity.CodeEntity;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;

@Service
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

    public ProductOptionFacadeImpl(ReadableProductOptionValueMapper readableOptionValueMapper,
                                   ProductOptionService productOptionService, ProductOptionValueService productOptionValueService,
                                   ReadableProductOptionMapper readableMapper, PersistableProductOptionMapper persistableeMapper,
                                   PersistableProductOptionValueMapper persistableOptionValueMapper,
                                   ProductAttributeService productAttributeService,
                                   PersistableProductAttributeMapper persistableProductAttributeMapper,
                                   ReadableProductAttributeMapper readableProductAttributeMapper, ProductService productService) {
        this.readableOptionValueMapper = readableOptionValueMapper;
        this.productOptionService = productOptionService;
        this.productOptionValueService = productOptionValueService;
        this.readableMapper = readableMapper;
        this.persistableeMapper = persistableeMapper;
        this.persistableOptionValueMapper = persistableOptionValueMapper;
        this.productAttributeService = productAttributeService;
        this.persistableProductAttributeMapper = persistableProductAttributeMapper;
        this.readableProductAttributeMapper = readableProductAttributeMapper;
        this.productService = productService;
    }

    @Override
    public ReadableProductOptionEntity saveOption(PersistableProductOptionEntity option, StoreMerchantId store,
                                                  LanguageCode language) {
        Assert.notNull(option, "ProductOption cannot be null");
        Assert.notNull(store, "store cannot be null");

        ProductOption optionModel = new ProductOption();
        if (option.getId() != null && option.getId() > 0) {
            optionModel = productOptionService.getById(store, option.getId());
            if (optionModel == null) {
                throw new ResourceNotFoundException(
                        "ProductOption not found for if [" + option.getId() + "] and store [" + store + "]");
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
            throw new ResourceNotFoundException(
                    "ProductOption not found for [" + optionId + "] and store [" + store + "]");
        }
        try {
            productOptionService.delete(optionModel);
        } catch (ServiceException e) {
            throw new ServiceRuntimeException("An exception occured while deleting ProductOption [" + optionId + "]",
                    e);
        }
    }

    @Override
    public void deleteOptionValue(Long optionValueId, StoreMerchantId store) {
        ProductOptionValue optionModel = productOptionValueService.getById(store, optionValueId);
        if (optionModel == null) {
            throw new ResourceNotFoundException(
                    "ProductOptionValue not found for  [" + optionValueId + "] and store [" + store + "]");
        }
        try {
            productOptionValueService.delete(optionModel);
        } catch (ServiceException e) {
            throw new ServiceRuntimeException(
                    "An exception occured while deleting ProductOptionValue [" + optionValueId + "]", e);
        }
    }

    @Override
    public ReadableProductOptionValueList optionValues(StoreMerchantId store, LanguageCode language, String name,
                                                       Pageable pageable) {
        Assert.notNull(store, "StoreMerchantId should not be null");

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
        Assert.notNull(store, "StoreMerchantId should not be null");

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

        Assert.notNull(optionId, "Option id cannot be null");
        Assert.notNull(store, "Store cannot be null");

        ProductOption option = productOptionService.getById(store, optionId);

        if (option == null) {
            throw new ResourceNotFoundException("Option id [" + optionId + "] not found");
        }

        return readableMapper.convert(option, store, language);
    }

    @Override
    public boolean optionExists(String code, StoreMerchantId store) {
        Assert.notNull(code, "Option code must not be null");
        Assert.notNull(store, "Store code must not be null");
        boolean exists = false;
        ProductOption option = productOptionService.getByCode(store, code);
        if (option != null) {
            exists = true;
        }
        return exists;
    }

    @Override
    public boolean optionValueExists(String code, StoreMerchantId store) {
        Assert.notNull(code, "Option value code must not be null");
        Assert.notNull(store, "Store code must not be null");
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
        Assert.notNull(optionValue, "Option value code must not be null");
        Assert.notNull(store, "Store code must not be null");

        ProductOptionValue value = new ProductOptionValue();
        if (optionValue.getId() != null && optionValue.getId() > 0) {
            value = productOptionValueService.getById(store, optionValue.getId());
            if (value == null) {
                throw new ResourceNotFoundException(
                        "ProductOptionValue [" + optionValue.getId() + "] does not exists for store [" + store + "]");
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

        Assert.notNull(optionValueId, "OptionValue id cannot be null");
        Assert.notNull(store, "Store cannot be null");

        ProductOptionValue optionValue = productOptionValueService.getById(store, optionValueId);

        if (optionValue == null) {
            throw new ResourceNotFoundException("OptionValue id [" + optionValueId + "] not found");
        }

        return readableOptionValueMapper.convert(optionValue, store, language);
    }

    @Override
    public ReadableProductAttributeEntity saveAttribute(Long productId, PersistableProductAttribute attribute,
                                                        StoreMerchantId store, LanguageCode language) {
        Assert.notNull(productId, "Product id cannot be null");
        Assert.notNull(attribute, "ProductAttribute cannot be null");
        Assert.notNull(attribute.getOption(), "ProductAttribute option cannot be null");
        Assert.notNull(attribute.getOptionValue(), "ProductAttribute option value cannot be null");
        Assert.notNull(store, "Store cannot be null");

        attribute.setProductId(productId);
        ProductAttribute attr = new ProductAttribute();
        if (attribute.getId() != null && attribute.getId() > 0) {
            attr = productAttributeService.getById(attribute.getId());
            if (attr == null) {
                throw new ResourceNotFoundException("Product attribute [" + attribute.getId() + "] not found");
            }

            if (productId != attr.getProduct().getId().longValue()) {
                throw new ResourceNotFoundException(
                        "Product attribute [" + attribute.getId() + "] not found for product [" + productId + "]");
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
            throw new ResourceNotFoundException(
                    "ProductAttribute not found for [" + attributeId + "] and store [" + store + "]");
        }

        if (attr.getProduct().getId().longValue() != productId) {
            throw new ResourceNotFoundException(
                    "ProductAttribute not found for [" + attributeId + "] and product [" + productId + "]");
        }

        if (!Objects.equals(attr.getProduct().getStore(), store)) {
            throw new ResourceNotFoundException("ProductAttribute not found for [" + attributeId + "] and product ["
                    + productId + "] and store [" + store + "]");
        }

        return readableProductAttributeMapper.convert(attr, store, language);
    }

    private Product product(long id, StoreMerchantId store) {
        Product product = productService.getById(id);

        if (product == null) {
            throw new ResourceNotFoundException("Productnot found for id [" + id + "]");
        }

        if (!Objects.equals(product.getStore(), store)) {
            throw new ResourceNotFoundException("Productnot found id [" + id + "] for store [" + store + "]");
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
                        "ProductAttribute not found for [" + attributeId + "] and store [" + store + "]");
            }

            if (attr.getProduct().getId().longValue() != productId) {
                throw new ResourceNotFoundException(
                        "ProductAttribute not found for [" + attributeId + "] and product [" + productId + "]");
            }

            if (!Objects.equals(attr.getProduct().getStore(), store)) {
                throw new ResourceNotFoundException("ProductAttribute not found for [" + attributeId + "] and product ["
                        + productId + "] and store [" + store + "]");
            }

            productAttributeService.delete(attr);

        } catch (ServiceException e) {
            throw new ServiceRuntimeException(
                    "An exception occured while deleting ProductAttribute [" + attributeId + "]", e);
        }
    }

    /*
     * @TODO not needed for now
     *
     * @Override public void addOptionValueImage(MultipartFile image, Long optionValueId,
     * StoreMerchantId store, LanguageCode language) {
     *
     *
     * Assert.notNull(optionValueId, "OptionValueId must not be null");
     * Assert.notNull(image, "Image must not be null"); //get option value
     * ProductOptionValue value = productOptionValueService.getById(store, optionValueId);
     * if (value == null) { throw new ResourceNotFoundException("Product option value [" +
     * optionValueId + "] not found"); }
     *
     * try { String imageName = image.getOriginalFilename(); InputStream inputStream =
     * image.getInputStream(); InputContentFile cmsContentImage = new InputContentFile();
     * cmsContentImage.setFileName(imageName);
     * cmsContentImage.setMimeType(image.getContentType());
     * cmsContentImage.setFile(inputStream);
     *
     * contentService.addOptionImage(store.getId(), cmsContentImage);
     * value.setProductOptionValueImage(imageName);
     * productOptionValueService.saveOrUpdate(value); } catch (Exception e) { throw new
     * ServiceRuntimeException("Exception while adding option value image", e); }
     *
     *
     * }
     *
     * @Override public void removeOptionValueImage(Long optionValueId, StoreMerchantId
     * store, LanguageCode language) { Assert.notNull(optionValueId,
     * "OptionValueId must not be null"); ProductOptionValue value =
     * productOptionValueService.getById(store, optionValueId); if (value == null) { throw
     * new ResourceNotFoundException("Product option value [" + optionValueId +
     * "] not found"); }
     *
     * try {
     *
     * contentService.removeFile(store.getId(), FileContentType.PROPERTY,
     * value.getProductOptionValueImage()); value.setProductOptionValueImage(null);
     * productOptionValueService.saveOrUpdate(value); } catch (Exception e) { throw new
     * ServiceRuntimeException("Exception while removing option value image", e); }
     *
     * }
     */
    @Override
    public List<CodeEntity> createAttributes(List<PersistableProductAttribute> attributes, Long productId,
                                             StoreMerchantId store) {
        Assert.notNull(productId, "Product id must not be null");
        Assert.notNull(store, "Merchant cannot be null");

        // convert to model

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
