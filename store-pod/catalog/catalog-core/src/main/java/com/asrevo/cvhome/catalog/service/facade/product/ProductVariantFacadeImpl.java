package com.asrevo.cvhome.catalog.service.facade.product;

import java.util.ArrayList;
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
import com.asrevo.cvhome.catalog.errors.InventoryNotConvertibleException;
import com.asrevo.cvhome.catalog.errors.ProductNotConvertibleException;
import com.asrevo.cvhome.catalog.errors.ProductNotFoundException;
import com.asrevo.cvhome.catalog.errors.ProductPriceNotConvertibleException;
import com.asrevo.cvhome.catalog.errors.ProductReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.errors.ProductVariantNotFoundException;
import com.asrevo.cvhome.catalog.errors.ProductVariantParentMissingException;
import com.asrevo.cvhome.catalog.errors.ProductVariantSkuConflictException;
import com.asrevo.cvhome.catalog.errors.ProductVariationOptionsIdenticalException;
import com.asrevo.cvhome.catalog.errors.ProductVariationReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.model.product.ReadableProduct;
import com.asrevo.cvhome.catalog.model.product.product.variant.PersistableProductVariant;
import com.asrevo.cvhome.catalog.model.product.product.variant.ReadableProductVariant;
import com.asrevo.cvhome.catalog.service.mapper.catalog.product.PersistableProductVariantMapper;
import com.asrevo.cvhome.catalog.service.mapper.catalog.product.ReadableProductVariantMapper;
import com.asrevo.cvhome.catalog.services.product.variant.ProductVariantService;
import com.asrevo.cvhome.catalog.services.product.variation.ProductVariationService;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.entity.ReadableEntityList;

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
    public ReadableProductVariant get(Long instanceId, Long productId, StoreMerchantId store, LanguageCode language)
            throws ProductVariantNotFoundException, ProductVariantParentMissingException, InventoryNotConvertibleException {
        Optional<ProductVariant> productVariant = this.getproductVariant(instanceId, productId, store);

        if (productVariant.isEmpty()) {
            throw ProductVariantNotFoundException.of(instanceId, store);
        }

        ProductVariant model = productVariant.get();
        return readableProductVariantMapper.convert(model, store, language);
    }

    @Override
    public boolean exists(String sku, StoreMerchantId store, Long productId, LanguageCode language)
            throws ProductNotFoundException, ProductNotConvertibleException,
            ProductPriceNotConvertibleException, ProductVariantParentMissingException,
            InventoryNotConvertibleException {
        ReadableProduct product = productCommonFacade.getProduct(store, productId, language);
        return productVariantService.exist(sku, product.getId());
    }

    @Override
    public Long create(PersistableProductVariant productVariant, Long productId, StoreMerchantId store,
                       LanguageCode language)

            throws ProductVariationOptionsIdenticalException, ProductVariationReferenceUnresolvableException,
            ProductReferenceUnresolvableException,
            ProductVariantSkuConflictException, ProductPriceNotConvertibleException,
            InventoryNotConvertibleException, ServiceException {
        if (productVariant.getVariation() != null && productVariant.getVariation() > 0
                && productVariant.getVariationValue() != null && productVariant.getVariationValue() > 0) {

            List<ProductVariation> variations = productVariationService
                    .getByIds(Arrays.asList(productVariant.getVariation(), productVariant.getVariationValue()), store);

            boolean differentOption = variations.stream()
                    .map(i -> i.getProductOption().getCode())
                    .distinct()
                    .count() > 1;

            if (!differentOption) {
                throw ProductVariationOptionsIdenticalException.of(productVariant.getVariation());
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
                       LanguageCode language)
            throws ProductVariantNotFoundException, ProductVariationReferenceUnresolvableException, ProductReferenceUnresolvableException,
            ProductVariantSkuConflictException, ProductPriceNotConvertibleException,
            InventoryNotConvertibleException, ServiceException {
        Optional<ProductVariant> instanceModel = this.getproductVariant(instanceId, productId, store);
        if (instanceModel.isEmpty()) {
            throw ProductVariantNotFoundException.of(instanceId, store);
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
    public void delete(Long productVariant, Long productId, StoreMerchantId store)
            throws ProductVariantNotFoundException, ServiceException {
        Optional<ProductVariant> instanceModel = this.getproductVariant(productVariant, productId, store);
        if (instanceModel.isEmpty()) {
            throw ProductVariantNotFoundException.of(productVariant, store);
        }

        productVariantService.delete(instanceModel.get());
    }

    @Override
    public ReadableEntityList<ReadableProductVariant> list(Long productId, StoreMerchantId store, LanguageCode language,
                                                           Pageable pageable)
            throws ProductNotFoundException, ProductVariantParentMissingException, InventoryNotConvertibleException {
        Product product = productFacade.getProduct(productId, store);

        if (product == null) {
            throw ProductNotFoundException.of(productId, store);
        }

        Page<ProductVariant> instances = productVariantService.getByProductId(store, product, language, pageable);

        // A plain loop rather than stream().map(...): the variant mapper declares checked failures now.
        List<ReadableProductVariant> readableInstances = new ArrayList<>();
        for (ProductVariant variant : instances) {
            readableInstances.add(readableProductVariantMapper.convert(variant, store, language));
        }

        return createReadableList(instances, readableInstances);
    }

}
