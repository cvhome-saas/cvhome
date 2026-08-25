package com.asrevo.cvhome.catalog.service.mapper.catalog.product;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.entity.product.availability.ProductAvailability;
import com.asrevo.cvhome.catalog.entity.product.variant.ProductVariant;
import com.asrevo.cvhome.catalog.entity.product.variation.ProductVariation;
import com.asrevo.cvhome.catalog.errors.InventoryNotConvertibleException;
import com.asrevo.cvhome.catalog.errors.ProductPriceNotConvertibleException;
import com.asrevo.cvhome.catalog.errors.ProductReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.errors.ProductVariantSkuConflictException;
import com.asrevo.cvhome.catalog.errors.ProductVariationReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.model.product.product.variant.PersistableProductVariant;
import com.asrevo.cvhome.catalog.services.product.ProductService;
import com.asrevo.cvhome.catalog.services.product.variation.ProductVariationService;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.mapper.Mapper;

@Component
public class PersistableProductVariantMapper implements Mapper<PersistableProductVariant, ProductVariant> {

    private final ProductVariationService productVariationService;

    private final PersistableProductAvailabilityMapper persistableProductAvailabilityMapper;

    private final ProductService productService;

    public PersistableProductVariantMapper(ProductVariationService productVariationService,
                                           PersistableProductAvailabilityMapper persistableProductAvailabilityMapper,
                                           ProductService productService) {
        this.productVariationService = productVariationService;
        this.persistableProductAvailabilityMapper = persistableProductAvailabilityMapper;
        this.productService = productService;
    }

    @Override
    public ProductVariant convert(PersistableProductVariant source, StoreMerchantId store, LanguageCode language)
            throws ProductVariationReferenceUnresolvableException, ProductReferenceUnresolvableException,
            ProductVariantSkuConflictException, ProductPriceNotConvertibleException,
            InventoryNotConvertibleException {
        ProductVariant productVariantModel = new ProductVariant();
        return this.merge(source, productVariantModel, store, language);
    }

    @Override
    public ProductVariant merge(PersistableProductVariant source, ProductVariant destination, StoreMerchantId store,
                                LanguageCode language)
            throws ProductVariationReferenceUnresolvableException, ProductReferenceUnresolvableException,
            ProductVariantSkuConflictException, ProductPriceNotConvertibleException,
            InventoryNotConvertibleException {

        Long productVariationValue = source.getVariationValue();

        Optional<ProductVariation> variation = resolveVariation(source, store);
        Optional<ProductVariation> variationValue = resolveVariationValue(source, store);

        if (variation.isEmpty()) {
            throw ProductVariationReferenceUnresolvableException.of(source.getVariation(), store);
        }

        destination.setVariation(variation.get());

        if (productVariationValue != null) {
            destination.setVariationValue(variationValue.orElse(null));
        }

        destination.setCode(buildInstanceCode(variation.get(), variationValue, productVariationValue));

        destination.setAvailable(source.isAvailable());
        destination.setDefaultSelection(source.isDefaultSelection());
        destination.setSku(source.getSku());

        applyDate(source, destination);

        destination.setSortOrder(source.getSortOrder());

        applyInventory(source, destination, store, language);
        applyProduct(source, destination, store);

        return destination;
    }

    private Optional<ProductVariation> resolveVariation(PersistableProductVariant source, StoreMerchantId store)
            throws ProductVariationReferenceUnresolvableException {
        if (StringUtils.isEmpty(source.getVariationCode())) {
            return productVariationService.getById(store, source.getVariation());
        }
        return productVariationService.getByCode(store, source.getVariationCode());
    }

    private Optional<ProductVariation> resolveVariationValue(PersistableProductVariant source, StoreMerchantId store)
            throws ProductVariationReferenceUnresolvableException {
        if (StringUtils.isEmpty(source.getVariationCode())) {
            return resolveVariationValueById(store, source.getVariationValue());
        }
        return resolveVariationValueByCode(store, source.getVariationValueCode(), source.getVariationValue());
    }

    private String buildInstanceCode(ProductVariation variation, Optional<ProductVariation> variationValue,
            Long productVariationValue) {
        StringBuilder instanceCode = new StringBuilder();
        instanceCode.append(variation.getCode());
        if (productVariationValue != null) {
            instanceCode.append(":").append(variationValue.get().getCode());
        }
        return instanceCode.toString();
    }

    private void applyDate(PersistableProductVariant source, ProductVariant destination)
            throws ProductPriceNotConvertibleException {
        if (Objects.nonNull(source.getDateAvailable())) {
            source.setDateAvailable(Instant.now());
        }

        if (source.getDateAvailable() != null) {
            try {
                destination.setDateAvailable(source.getDateAvailable());
            } catch (Exception e) {
                throw ProductPriceNotConvertibleException.of(source.getDateAvailable(), e);
            }
        }
    }

    private void applyInventory(PersistableProductVariant source, ProductVariant destination, StoreMerchantId store,
                                LanguageCode language)
            throws InventoryNotConvertibleException {
        if (source.getInventory() != null) {
            ProductAvailability availability = persistableProductAvailabilityMapper.convert(source.getInventory(),
                    store, language);
            availability.setProductVariant(destination);
            destination.getAvailabilities().add(availability);
        }
    }

    private void applyProduct(PersistableProductVariant source, ProductVariant destination, StoreMerchantId store)
            throws ProductReferenceUnresolvableException, ProductVariantSkuConflictException {
        if (source.getProductId() == null || source.getProductId() <= 0) {
            return;
        }
        Product product = productService.findOne(source.getProductId(), store);

        if (product == null || !product.getStore().equals(store)) {
            throw ProductReferenceUnresolvableException.of(source.getProductId(), store);
        }

        if (product.getSku() != null && product.getSku().equals(source.getSku())) {
            throw ProductVariantSkuConflictException.of(source.getSku(), product.getSku());
        }

        destination.setProduct(product);
    }

    private Optional<ProductVariation> resolveVariationValueById(StoreMerchantId store, Long productVariationValue)
            throws ProductVariationReferenceUnresolvableException {
        if (productVariationValue == null) {
            return Optional.empty();
        }
        Optional<ProductVariation> variationValue = productVariationService.getById(store, productVariationValue);
        if (variationValue.isEmpty()) {
            throw ProductVariationReferenceUnresolvableException.of(productVariationValue, store);
        }
        return variationValue;
    }

    private Optional<ProductVariation> resolveVariationValueByCode(StoreMerchantId store, String productVariationValueCode,
                                                                   Long productVariationValue)
            throws ProductVariationReferenceUnresolvableException {
        if (productVariationValueCode == null) {
            return Optional.empty();
        }
        Optional<ProductVariation> variationValue = productVariationService.getByCode(store, productVariationValueCode);
        if (variationValue.isEmpty()) {
            throw ProductVariationReferenceUnresolvableException.of(productVariationValueCode, store);
        }
        return variationValue;
    }

}
