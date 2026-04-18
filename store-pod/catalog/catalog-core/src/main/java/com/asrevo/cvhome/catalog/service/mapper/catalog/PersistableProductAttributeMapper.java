package com.asrevo.cvhome.catalog.service.mapper.catalog;

import java.util.Objects;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.entity.product.attribute.ProductAttribute;
import com.asrevo.cvhome.catalog.entity.product.attribute.ProductOption;
import com.asrevo.cvhome.catalog.entity.product.attribute.ProductOptionValue;
import com.asrevo.cvhome.catalog.model.product.attribute.PersistableProductAttribute;
import com.asrevo.cvhome.catalog.services.product.ProductService;
import com.asrevo.cvhome.catalog.services.product.attribute.ProductOptionService;
import com.asrevo.cvhome.catalog.services.product.attribute.ProductOptionValueService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.controller.exception.ConversionRuntimeException;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.mapper.Mapper;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;

@Component
public class PersistableProductAttributeMapper implements Mapper<PersistableProductAttribute, ProductAttribute> {

    private final ProductOptionService productOptionService;

    private final ProductOptionValueService productOptionValueService;

    private final ProductService productService;

    private final PersistableProductOptionValueMapper persistableProductOptionValueMapper;

    public PersistableProductAttributeMapper(ProductOptionService productOptionService,
                                             ProductOptionValueService productOptionValueService, ProductService productService,
                                             PersistableProductOptionValueMapper persistableProductOptionValueMapper) {
        this.productOptionService = productOptionService;
        this.productOptionValueService = productOptionValueService;
        this.productService = productService;
        this.persistableProductOptionValueMapper = persistableProductOptionValueMapper;
    }

    @Override
    public ProductAttribute convert(PersistableProductAttribute source, StoreMerchantId store, LanguageCode language) {
        ProductAttribute attribute = new ProductAttribute();
        return merge(source, attribute, store, language);
    }

    @Override
    public ProductAttribute merge(PersistableProductAttribute source, ProductAttribute destination,
                                  StoreMerchantId store, LanguageCode language) {

        ProductOption productOption;

        if (!StringUtils.isBlank(source.getOption().getCode())) {
            productOption = productOptionService.getByCode(store, source.getOption().getCode());
        } else {
            productOption = productOptionService.getById(source.getOption().getId());
        }

        if (productOption == null) {
            throw new ConversionRuntimeException("Product option id " + source.getOption().getId() + " does not exist");
        }

        ProductOptionValue productOptionValue;

        if (!StringUtils.isBlank(source.getOptionValue().getCode())) {
            productOptionValue = productOptionValueService.getByCode(store, source.getOptionValue().getCode());
        } else if (source.getProductId() != null && source.getOptionValue().getId() > 0) {
            productOptionValue = productOptionValueService.getById(source.getOptionValue().getId());
        } else {
            // ProductOption value is text
            productOptionValue = new ProductOptionValue();
            productOptionValue.setProductOptionDisplayOnly(true);
            productOptionValue.setCode(UUID.randomUUID().toString());
            productOptionValue.setStoreMerchantId(store);
        }

        if (!CollectionUtils.isEmpty((source.getOptionValue().getDescriptions()))) {
            productOptionValue = persistableProductOptionValueMapper.merge(source.getOptionValue(), productOptionValue,
                    store, language);
            try {
                productOptionValueService.saveOrUpdate(productOptionValue);
            } catch (ServiceException e) {
                throw new ConversionRuntimeException("Error converting ProductOptionValue", e);
            }
        }

        if (productOptionValue == null && !source.isAttributeDisplayOnly()) {
            throw new ConversionRuntimeException(
                    "Product option value id " + source.getOptionValue().getId() + " does not exist");
        }

        if (!Objects.equals(productOption.getStoreMerchantId(), store)) {
            throw new ConversionRuntimeException("Invalid product option id ");
        }

        if (productOptionValue != null && !Objects.equals(productOptionValue.getStoreMerchantId(), store)) {
            throw new ConversionRuntimeException("Invalid product option value id ");
        }

        if (source.getProductId() != null && source.getProductId() > 0) {
            Product p = productService.getById(source.getProductId());
            if (p == null) {
                throw new ConversionRuntimeException("Invalid product id ");
            }
            destination.setProduct(p);
        }

        if (destination.getId() != null && destination.getId() > 0) {
            destination.setId(destination.getId());
        } else {
            destination.setId(null);
        }

        destination.setProductOption(productOption);
        destination.setProductOptionValue(productOptionValue);
        destination.setProductAttributePrice(source.getProductAttributePrice());
        destination.setProductAttributeWeight(source.getProductAttributeWeight());
        destination.setProductAttributePrice(source.getProductAttributePrice());
        destination.setAttributeDisplayOnly(source.isAttributeDisplayOnly());

        return destination;
    }

}
