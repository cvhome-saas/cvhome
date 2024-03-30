package com.asrevo.cvhome.store.service.facade.product;

import com.asrevo.cvhome.store.controller.exception.ConstraintException;
import com.asrevo.cvhome.store.controller.exception.ResourceNotFoundException;
import com.asrevo.cvhome.store.controller.exception.ServiceRuntimeException;
import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import com.asrevo.cvhome.store.core.entity.catalog.product.variant.ProductVariant;
import com.asrevo.cvhome.store.core.entity.catalog.product.variation.ProductVariation;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.catalog.product.ReadableProduct;
import com.asrevo.cvhome.store.core.model.catalog.product.product.variant.PersistableProductVariant;
import com.asrevo.cvhome.store.core.model.catalog.product.product.variant.ReadableProductVariant;
import com.asrevo.cvhome.store.core.model.entity.ReadableEntityList;
import com.asrevo.cvhome.store.core.services.catalog.product.variant.ProductVariantService;
import com.asrevo.cvhome.store.core.services.catalog.product.variation.ProductVariationService;
import com.asrevo.cvhome.store.service.mapper.catalog.product.PersistableProductVariantMapper;
import com.asrevo.cvhome.store.service.mapper.catalog.product.ReadableProductVariantMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.asrevo.cvhome.store.utils.ReadableEntityUtil.createReadableList;


/**
 * Product instance management facade
 *
 * @author carlsamson
 */
@Component
public class ProductVariantFacadeImpl implements ProductVariantFacade {


    @Autowired
    private ReadableProductVariantMapper readableProductVariantMapper;

    @Autowired
    private PersistableProductVariantMapper persistableProductVariantMapper;

    @Autowired
    private ProductVariantService productVariantService;


    @Autowired
    private ProductVariationService productVariationService;

    @Autowired
    private ProductFacade productFacade;

    @Autowired
    private ProductCommonFacade productCommonFacade;

    @Override
    public ReadableProductVariant get(Long instanceId, Long productId, MerchantStore store, Language language) {
        Optional<ProductVariant> productVariant = this.getproductVariant(instanceId, productId, store);

        if (productVariant.isEmpty()) {
            throw new ResourceNotFoundException("Product instance + [" + instanceId + "] not found for store [" + store.getCode() + "]");
        }


        ProductVariant model = productVariant.get();
        return readableProductVariantMapper.convert(model, store, language);


    }

    @Override
    public boolean exists(String sku, MerchantStore store, Long productId, Language language) {
        ReadableProduct product = null;
        try {
            product = productCommonFacade.getProduct(store, productId, language);
        } catch (Exception e) {
            throw new ServiceRuntimeException("Error while getting product [" + productId + "]", e);
        }

        return productVariantService.exist(sku, product.getId());
    }

    @Override
    public Long create(PersistableProductVariant productVariant, Long productId, MerchantStore store,
                       Language language) {
        Assert.notNull(store, "MerchantStore cannot be null");
        Assert.notNull(productVariant, "productVariant cannot be null");
        Assert.notNull(productId, "Product id cannot be null");

        //variation and variation value should not be of same product option code
        if (
                productVariant.getVariation() != null && productVariant.getVariation().longValue() > 0 &&
                        productVariant.getVariationValue() != null && productVariant.getVariationValue().longValue() > 0) {

            List<ProductVariation> variations = productVariationService.getByIds(Arrays.asList(productVariant.getVariation(), productVariant.getVariationValue()), store);

            boolean differentOption = variations.stream().map(i -> i.getProductOption().getCode()).distinct().count() > 1;

            if (!differentOption) {
                throw new ConstraintException("Product option of instance.variant and instance.variantValue must be different");
            }

        }


        productVariant.setProductId(productId);
        productVariant.setId(null);
        ProductVariant variant = persistableProductVariantMapper.convert(productVariant, store, language);

        try {
            productVariantService.saveProductVariant(variant);
        } catch (ServiceException e) {
            throw new ServiceRuntimeException("Cannot save product instance for store [" + store.getCode() + "] and productId [" + productId + "]", e);
        }

        return variant.getId();
    }

    @Override
    public void update(Long instanceId, PersistableProductVariant productVariant, Long productId, MerchantStore store, Language language) {
        Assert.notNull(store, "MerchantStore cannot be null");
        Assert.notNull(productVariant, "productVariant cannot be null");
        Assert.notNull(productId, "Product id cannot be null");
        Assert.notNull(instanceId, "Product instance id cannot be null");

        Optional<ProductVariant> instanceModel = this.getproductVariant(instanceId, productId, store);
        if (instanceModel.isEmpty()) {
            throw new ResourceNotFoundException("productVariant with id [" + instanceId + "] not found for store [" + store.getCode() + "] and productId [" + productId + "]");
        }

        productVariant.setProductId(productId);

        ProductVariant mergedModel = persistableProductVariantMapper.merge(productVariant, instanceModel.get(), store, language);
        try {
            productVariantService.saveProductVariant(mergedModel);
        } catch (ServiceException e) {
            throw new ServiceRuntimeException("Cannot save product instance for store [" + store.getCode() + "] and productId [" + productId + "]", e);
        }

    }

    private Optional<ProductVariant> getproductVariant(Long id, Long productId, MerchantStore store) {
        return productVariantService.getById(id, productId, store);
    }


    @Override
    public void delete(Long productVariant, Long productId, MerchantStore store) {
        Assert.notNull(store, "MerchantStore cannot be null");
        Assert.notNull(productVariant, "productVariant id cannot be null");
        Assert.notNull(productId, "Product id cannot be null");


        Optional<ProductVariant> instanceModel = this.getproductVariant(productVariant, productId, store);
        if (instanceModel.isEmpty()) {
            throw new ResourceNotFoundException("productVariant with id [" + productVariant + "] not found for store [" + store.getCode() + "] and productId [" + productId + "]");
        }

        try {
            productVariantService.delete(instanceModel.get());
        } catch (ServiceException e) {
            throw new ServiceRuntimeException("Cannot delete product instance [" + productVariant + "]  for store [" + store.getCode() + "] and productId [" + productId + "]", e);
        }

    }

    @Override
    public ReadableEntityList<ReadableProductVariant> list(Long productId, MerchantStore store, Language language,
                                                           int page, int count) {
        Assert.notNull(store, "MerchantStore cannot be null");
        Assert.notNull(productId, "Product id cannot be null");

        Product product = productFacade.getProduct(productId, store);

        if (product == null) {
            throw new ResourceNotFoundException("Product with id [" + productId + "] not found for store [" + store.getCode() + "]");
        }

        Page<ProductVariant> instances = productVariantService.getByProductId(store, product, language, page, count);


        List<ReadableProductVariant> readableInstances = instances.stream()
                .map(rp -> this.readableProductVariantMapper.convert(rp, store, language)).collect(Collectors.toList());


        return createReadableList(instances, readableInstances);

    }


}
