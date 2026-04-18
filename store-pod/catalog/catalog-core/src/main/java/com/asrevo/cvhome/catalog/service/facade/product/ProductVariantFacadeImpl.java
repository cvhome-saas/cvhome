package com.asrevo.cvhome.catalog.service.facade.product;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.entity.product.variant.ProductVariant;
import com.asrevo.cvhome.catalog.entity.product.variation.ProductVariation;
import com.asrevo.cvhome.catalog.model.product.ReadableProduct;
import com.asrevo.cvhome.catalog.model.product.product.variant.PersistableProductVariant;
import com.asrevo.cvhome.catalog.model.product.product.variant.ReadableProductVariant;
import com.asrevo.cvhome.catalog.service.mapper.catalog.product.PersistableProductVariantMapper;
import com.asrevo.cvhome.catalog.service.mapper.catalog.product.ReadableProductVariantMapper;
import com.asrevo.cvhome.catalog.services.product.variant.ProductVariantService;
import com.asrevo.cvhome.catalog.services.product.variation.ProductVariationService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.controller.exception.ConstraintException;
import com.asrevo.cvhome.store.controller.exception.ResourceNotFoundException;
import com.asrevo.cvhome.store.controller.exception.ServiceRuntimeException;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.entity.ReadableEntityList;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;

import static com.asrevo.cvhome.store.utils.ReadableEntityUtil.createReadableList;

/**
 * Product instance management facade
 *
 * @author carlsamson
 */
@Component
public class ProductVariantFacadeImpl implements ProductVariantFacade {

    private final ReadableProductVariantMapper readableProductVariantMapper;

    private final PersistableProductVariantMapper persistableProductVariantMapper;

    private final ProductVariantService productVariantService;

    private final ProductVariationService productVariationService;

    private final ProductFacade productFacade;

    private final ProductCommonFacade productCommonFacade;

    public ProductVariantFacadeImpl(ReadableProductVariantMapper readableProductVariantMapper,
                                    PersistableProductVariantMapper persistableProductVariantMapper,
                                    ProductVariantService productVariantService, ProductVariationService productVariationService,
                                    @Qualifier("productFacade") ProductFacade productFacade, ProductCommonFacade productCommonFacade) {
        this.readableProductVariantMapper = readableProductVariantMapper;
        this.persistableProductVariantMapper = persistableProductVariantMapper;
        this.productVariantService = productVariantService;
        this.productVariationService = productVariationService;
        this.productFacade = productFacade;
        this.productCommonFacade = productCommonFacade;
    }

    @Override
    public ReadableProductVariant get(Long instanceId, Long productId, StoreMerchantId store, LanguageCode language) {
        Optional<ProductVariant> productVariant = this.getproductVariant(instanceId, productId, store);

        if (productVariant.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Product instance + [" + instanceId + "] not found for store [" + store + "]");
        }

        ProductVariant model = productVariant.get();
        return readableProductVariantMapper.convert(model, store, language);
    }

    @Override
    public boolean exists(String sku, StoreMerchantId store, Long productId, LanguageCode language) {
        ReadableProduct product;
        try {
            product = productCommonFacade.getProduct(store, productId, language);
        } catch (Exception e) {
            throw new ServiceRuntimeException("Error while getting product [" + productId + "]", e);
        }

        return productVariantService.exist(sku, product.getId());
    }

    @Override
    public Long create(PersistableProductVariant productVariant, Long productId, StoreMerchantId store,
                       LanguageCode language) {
        if (productVariant.getVariation() != null && productVariant.getVariation() > 0
                && productVariant.getVariationValue() != null && productVariant.getVariationValue() > 0) {

            List<ProductVariation> variations = productVariationService
                    .getByIds(Arrays.asList(productVariant.getVariation(), productVariant.getVariationValue()), store);

            boolean differentOption = variations.stream()
                    .map(i -> i.getProductOption().getCode())
                    .distinct()
                    .count() > 1;

            if (!differentOption) {
                throw new ConstraintException(
                        "Product option of instance.variant and instance.variantValue must be" + " different");
            }
        }

        productVariant.setProductId(productId);
        productVariant.setId(null);
        ProductVariant variant = persistableProductVariantMapper.convert(productVariant, store, language);

        productVariantService.saveProductVariant(variant);

        return variant.getId();
    }

    @Override
    public void update(Long instanceId, PersistableProductVariant productVariant, Long productId, StoreMerchantId store,
                       LanguageCode language) {
        Optional<ProductVariant> instanceModel = this.getproductVariant(instanceId, productId, store);
        if (instanceModel.isEmpty()) {
            throw new ResourceNotFoundException("productVariant with id [" + instanceId + "] not found for store ["
                    + store + "] and productId [" + productId + "]");
        }

        productVariant.setProductId(productId);

        ProductVariant mergedModel = persistableProductVariantMapper.merge(productVariant, instanceModel.get(), store,
                language);
        productVariantService.saveProductVariant(mergedModel);
    }

    private Optional<ProductVariant> getproductVariant(Long id, Long productId, StoreMerchantId store) {
        return productVariantService.getById(id, productId, store);
    }

    @Override
    public void delete(Long productVariant, Long productId, StoreMerchantId store) {
        Optional<ProductVariant> instanceModel = this.getproductVariant(productVariant, productId, store);
        if (instanceModel.isEmpty()) {
            throw new ResourceNotFoundException("productVariant with id [" + productVariant + "] not found for store ["
                    + store + "] and productId [" + productId + "]");
        }

        try {
            productVariantService.delete(instanceModel.get());
        } catch (ServiceException e) {
            throw new ServiceRuntimeException("Cannot delete product instance [" + productVariant + "]  for store ["
                    + store + "] and productId [" + productId + "]", e);
        }
    }

    @Override
    public ReadableEntityList<ReadableProductVariant> list(Long productId, StoreMerchantId store, LanguageCode language,
                                                           Pageable pageable) {
        Product product = productFacade.getProduct(productId, store);

        if (product == null) {
            throw new ResourceNotFoundException(
                    "Product with id [" + productId + "] not found for store [" + store + "]");
        }

        Page<ProductVariant> instances = productVariantService.getByProductId(store, product, language, pageable);

        List<ReadableProductVariant> readableInstances = instances.stream()
                .map(rp -> this.readableProductVariantMapper.convert(rp, store, language))
                .toList();

        return createReadableList(instances, readableInstances);
    }

}
