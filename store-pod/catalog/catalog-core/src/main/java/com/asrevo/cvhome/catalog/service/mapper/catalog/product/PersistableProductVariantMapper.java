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
import com.asrevo.cvhome.catalog.model.product.product.variant.PersistableProductVariant;
import com.asrevo.cvhome.catalog.services.product.ProductService;
import com.asrevo.cvhome.catalog.services.product.variation.ProductVariationService;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.controller.exception.OperationNotAllowedException;
import com.asrevo.cvhome.store.controller.exception.ResourceNotFoundException;
import com.asrevo.cvhome.store.controller.exception.ServiceRuntimeException;
import com.asrevo.cvhome.store.core.mapper.Mapper;

@Component
public class PersistableProductVariantMapper implements Mapper<PersistableProductVariant, ProductVariant> {

    private static final String PRODUCT_VARIATION_VALUE_PREFIX = "ProductVaritionValue [";

    private static final String NOT_FOUND_FOR_STORE = "] + not found for store [";

    private static final String CLOSE_BRACKET = "]";

    private static final String PRODUCT_PREFIX = "Product [";

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
    public ProductVariant convert(PersistableProductVariant source, StoreMerchantId store, LanguageCode language) {
        ProductVariant productVariantModel = new ProductVariant();
        return this.merge(source, productVariantModel, store, language);
    }

    @Override
    public ProductVariant merge(PersistableProductVariant source, ProductVariant destination, StoreMerchantId store,
                                LanguageCode language) {

        //
        Long productVariation = source.getVariation();
        Long productVariationValue = source.getVariationValue();

        String productVariationCode = source.getVariationCode();
        String productVariationValueCode = source.getVariationValueCode();

        Optional<ProductVariation> variation;
        Optional<ProductVariation> variationValue = Optional.empty();

        if (StringUtils.isEmpty(productVariationCode)) {

            variation = productVariationService.getById(store, productVariation);
            variationValue = resolveVariationValueById(store, productVariationValue);
        } else {
            variation = productVariationService.getByCode(store, productVariationCode);
            variationValue = resolveVariationValueByCode(store, productVariationValueCode, productVariationValue);
        }

        if (variation.isEmpty()) {
            throw new ResourceNotFoundException(
                    "ProductVarition [" + productVariation + NOT_FOUND_FOR_STORE + store + CLOSE_BRACKET);
        }

        destination.setVariation(variation.get());

        if (productVariationValue != null) {
            destination.setVariationValue(variationValue.orElse(null));
        }

        StringBuilder instanceCode = new StringBuilder();
        instanceCode.append(variation.get().getCode());
        if (productVariationValue != null) {
            instanceCode.append(":").append(variationValue.get().getCode());
        }

        destination.setCode(instanceCode.toString());

        destination.setAvailable(source.isAvailable());
        destination.setDefaultSelection(source.isDefaultSelection());
        destination.setSku(source.getSku());

        if (Objects.nonNull(source.getDateAvailable())) {
            source.setDateAvailable(Instant.now());
        }

        if (source.getDateAvailable() != null) {
            try {
                destination.setDateAvailable(source.getDateAvailable());
            } catch (Exception _) {
                throw new ServiceRuntimeException("Cant format date [" + source.getDateAvailable() + CLOSE_BRACKET);
            }
        }

        destination.setSortOrder(source.getSortOrder());

        if (source.getInventory() != null) {
            ProductAvailability availability = persistableProductAvailabilityMapper.convert(source.getInventory(),
                    store, language);
            availability.setProductVariant(destination);
            destination.getAvailabilities().add(availability);
        }

        Product product;

        if (source.getProductId() != null && source.getProductId() > 0) {
            product = productService.findOne(source.getProductId(), store);

            if (product == null) {
                throw new ResourceNotFoundException(
                        PRODUCT_PREFIX + source.getId() + NOT_FOUND_FOR_STORE + store + CLOSE_BRACKET);
            }

            if (!product.getStore().equals(store)) {
                throw new ResourceNotFoundException(
                        PRODUCT_PREFIX + source.getId() + NOT_FOUND_FOR_STORE + store + CLOSE_BRACKET);
            }

            if (product.getSku() != null && product.getSku().equals(source.getSku())) {
                throw new OperationNotAllowedException("Product variant sku [" + source.getSku()
                        + "] + must be different than product instance sku [" + product.getSku() + CLOSE_BRACKET);
            }

            destination.setProduct(product);
        }

        return destination;
    }

    private Optional<ProductVariation> resolveVariationValueById(StoreMerchantId store, Long productVariationValue) {
        if (productVariationValue == null) {
            return Optional.empty();
        }
        Optional<ProductVariation> variationValue = productVariationService.getById(store, productVariationValue);
        if (variationValue.isEmpty()) {
            throw new ResourceNotFoundException(
                    PRODUCT_VARIATION_VALUE_PREFIX + productVariationValue + NOT_FOUND_FOR_STORE + store + CLOSE_BRACKET);
        }
        return variationValue;
    }

    private Optional<ProductVariation> resolveVariationValueByCode(StoreMerchantId store, String productVariationValueCode,
                                                                   Long productVariationValue) {
        if (productVariationValueCode == null) {
            return Optional.empty();
        }
        Optional<ProductVariation> variationValue = productVariationService.getByCode(store, productVariationValueCode);
        if (variationValue.isEmpty()) {
            throw new ResourceNotFoundException(
                    PRODUCT_VARIATION_VALUE_PREFIX + productVariationValue + NOT_FOUND_FOR_STORE + store + CLOSE_BRACKET);
        }
        return variationValue;
    }

}
