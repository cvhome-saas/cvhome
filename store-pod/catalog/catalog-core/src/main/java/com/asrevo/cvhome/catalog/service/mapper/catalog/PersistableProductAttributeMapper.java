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
import com.asrevo.cvhome.catalog.errors.ProductAttributeNotConvertibleException;
import com.asrevo.cvhome.catalog.errors.ProductOptionNotConvertibleException;
import com.asrevo.cvhome.catalog.errors.ProductOptionReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.errors.ProductOptionValueReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.errors.ProductReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.model.product.attribute.PersistableProductAttribute;
import com.asrevo.cvhome.catalog.services.product.ProductService;
import com.asrevo.cvhome.catalog.services.product.attribute.ProductOptionService;
import com.asrevo.cvhome.catalog.services.product.attribute.ProductOptionValueService;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.mapper.Mapper;

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
    public ProductAttribute convert(PersistableProductAttribute source, StoreMerchantId store, LanguageCode language)
            throws ProductOptionReferenceUnresolvableException, ProductOptionValueReferenceUnresolvableException,
            ProductReferenceUnresolvableException, ProductAttributeNotConvertibleException,
            ProductOptionNotConvertibleException {
        ProductAttribute attribute = new ProductAttribute();
        return merge(source, attribute, store, language);
    }

    @Override
    public ProductAttribute merge(PersistableProductAttribute source, ProductAttribute destination,
                                  StoreMerchantId store, LanguageCode language)
            throws ProductOptionReferenceUnresolvableException, ProductOptionValueReferenceUnresolvableException,
            ProductReferenceUnresolvableException, ProductAttributeNotConvertibleException,
            ProductOptionNotConvertibleException {

        ProductOption productOption = resolveProductOption(source, store);
        ProductOptionValue productOptionValue = resolveProductOptionValue(source, store, language);

        validateOptionValue(source, productOptionValue, store);
        validateOwnership(productOption, productOptionValue, store);
        applyProduct(source, destination, store);

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

    private ProductOption resolveProductOption(PersistableProductAttribute source, StoreMerchantId store)
            throws ProductOptionReferenceUnresolvableException {
        ProductOption productOption;
        if (!StringUtils.isBlank(source.getOption().getCode())) {
            productOption = productOptionService.getByCode(store, source.getOption().getCode());
        } else {
            productOption = productOptionService.getById(source.getOption().getId());
        }
        if (productOption == null) {
            throw ProductOptionReferenceUnresolvableException.of(source.getOption().getId(), store);
        }
        return productOption;
    }

    private ProductOptionValue resolveProductOptionValue(PersistableProductAttribute source, StoreMerchantId store,
                                                         LanguageCode language)
            throws ProductAttributeNotConvertibleException, ProductOptionNotConvertibleException {
        ProductOptionValue productOptionValue = findOrBuildProductOptionValue(source, store);
        if (!CollectionUtils.isEmpty(source.getOptionValue().getDescriptions())) {
            productOptionValue = persistableProductOptionValueMapper.merge(source.getOptionValue(), productOptionValue,
                    store, language);
            productOptionValueService.saveOrUpdate(productOptionValue);
        }
        return productOptionValue;
    }

    private ProductOptionValue findOrBuildProductOptionValue(PersistableProductAttribute source, StoreMerchantId store) {
        if (!StringUtils.isBlank(source.getOptionValue().getCode())) {
            return productOptionValueService.getByCode(store, source.getOptionValue().getCode());
        }
        if (source.getProductId() != null && source.getOptionValue().getId() > 0) {
            return productOptionValueService.getById(source.getOptionValue().getId());
        }
        // ProductOption value is text
        ProductOptionValue productOptionValue = new ProductOptionValue();
        productOptionValue.setProductOptionDisplayOnly(true);
        productOptionValue.setCode(UUID.randomUUID().toString());
        productOptionValue.setStoreMerchantId(store);
        return productOptionValue;
    }

    private void validateOptionValue(PersistableProductAttribute source, ProductOptionValue productOptionValue,
                                     StoreMerchantId store) throws ProductOptionValueReferenceUnresolvableException {
        if (productOptionValue == null && !source.isAttributeDisplayOnly()) {
            throw ProductOptionValueReferenceUnresolvableException.of(source.getOptionValue().getId(), store);
        }
    }

    /**
     * Rejects an option or option value that belongs to another store.
     *
     * <p>
     * Reported as not-found rather than forbidden, for the same reason an order belonging to another customer is: a
     * 403 would confirm the id is real, which is what someone walking option ids wants to learn. The legacy message
     * ("Invalid product option id") said neither.
     * </p>
     */
    private void validateOwnership(ProductOption productOption, ProductOptionValue productOptionValue,
                                   StoreMerchantId store)
            throws ProductOptionReferenceUnresolvableException, ProductOptionValueReferenceUnresolvableException {
        if (!Objects.equals(productOption.getStoreMerchantId(), store)) {
            throw ProductOptionReferenceUnresolvableException.of(productOption.getId(), store);
        }
        if (productOptionValue != null && !Objects.equals(productOptionValue.getStoreMerchantId(), store)) {
            throw ProductOptionValueReferenceUnresolvableException.of(productOptionValue.getId(), store);
        }
    }

    private void applyProduct(PersistableProductAttribute source, ProductAttribute destination, StoreMerchantId store)
            throws ProductReferenceUnresolvableException {
        if (source.getProductId() == null || source.getProductId() <= 0) {
            return;
        }
        Product p = productService.getById(source.getProductId());
        if (p == null) {
            throw ProductReferenceUnresolvableException.of(source.getProductId(), store);
        }
        destination.setProduct(p);
    }

}
