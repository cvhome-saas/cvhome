package com.asrevo.cvhome.store.service.facade.product;

import static com.asrevo.cvhome.store.utils.NumberUtils.isPositive;

import com.asrevo.cvhome.store.controller.exception.ConversionException;
import com.asrevo.cvhome.store.controller.exception.ResourceNotFoundException;
import com.asrevo.cvhome.store.controller.exception.ServiceRuntimeException;
import com.asrevo.cvhome.store.core.entity.catalog.product.price.ProductPrice;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.catalog.product.PersistableProductPrice;
import com.asrevo.cvhome.store.core.model.catalog.product.ReadableProductPrice;
import com.asrevo.cvhome.store.core.services.catalog.pricing.PricingService;
import com.asrevo.cvhome.store.core.services.catalog.product.availability.ProductAvailabilityService;
import com.asrevo.cvhome.store.core.services.catalog.product.price.ProductPriceService;
import com.asrevo.cvhome.store.service.mapper.inventory.PersistableProductPriceMapper;
import com.asrevo.cvhome.store.service.populator.catalog.ReadableProductPricePopulator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class ProductPriceFacadeImpl implements ProductPriceFacade {

    private final ProductPriceService productPriceService;

    private final PricingService pricingService;

    private final PersistableProductPriceMapper persistableProductPriceMapper;

    public ProductPriceFacadeImpl(
            ProductPriceService productPriceService,
            PricingService pricingService,
            ProductAvailabilityService productAvailabilityService,
            PersistableProductPriceMapper persistableProductPriceMapper) {
        this.productPriceService = productPriceService;
        this.pricingService = pricingService;
        this.persistableProductPriceMapper = persistableProductPriceMapper;
    }

    @Override
    public Long save(PersistableProductPrice price, MerchantStore store) {

        ProductPrice productPrice =
                persistableProductPriceMapper.convert(price, store, store.getDefaultLanguage());
        try {
            if (!isPositive(productPrice.getId())) {
                // avoid detached entity failed to persist
                productPrice.getProductAvailability().setPrices(null);
                productPrice = productPriceService.saveOrUpdate(productPrice);
            } else {
                productPrice = productPriceService.saveOrUpdate(productPrice);
            }

        } catch (ServiceException e) {
            throw new ServiceRuntimeException("An exception occured while creating a ProductPrice");
        }
        return productPrice.getId();
    }

    @Override
    public List<ReadableProductPrice> list(
            String sku, Long inventoryId, MerchantStore store, Language language) {
        Assert.notNull(store, "MerchantStore cannot be null");
        Assert.notNull(sku, "Product sku cannot be null");
        Assert.notNull(inventoryId, "Product inventory cannot be null");

        List<ProductPrice> prices = productPriceService.findByInventoryId(inventoryId, sku, store);
        List<ReadableProductPrice> returnPrices =
                prices.stream()
                        .map(
                                p -> {
                                    try {
                                        return this.readablePrice(p, store, language);
                                    } catch (ConversionException e) {
                                        throw new ServiceRuntimeException(
                                                "An exception occured while getting product price"
                                                        + " for sku ["
                                                        + sku
                                                        + "] and Store ["
                                                        + store.getCode()
                                                        + "]",
                                                e);
                                    }
                                })
                        .collect(Collectors.toList());

        return returnPrices;
    }

    @Override
    public List<ReadableProductPrice> list(String sku, MerchantStore store, Language language) {
        Assert.notNull(store, "MerchantStore cannot be null");
        Assert.notNull(sku, "Product sku cannot be null");

        List<ProductPrice> prices = productPriceService.findByProductSku(sku, store);
        List<ReadableProductPrice> returnPrices =
                prices.stream()
                        .map(
                                p -> {
                                    try {
                                        return this.readablePrice(p, store, language);
                                    } catch (ConversionException e) {
                                        throw new ServiceRuntimeException(
                                                "An exception occured while getting product price"
                                                        + " for sku ["
                                                        + sku
                                                        + "] and Store ["
                                                        + store.getCode()
                                                        + "]",
                                                e);
                                    }
                                })
                        .collect(Collectors.toList());

        return returnPrices;
    }

    @Override
    public void delete(Long priceId, String sku, MerchantStore store) {
        Assert.notNull(priceId, "Product Price id cannot be null");
        Assert.notNull(store, "MerchantStore cannot be null");
        Assert.notNull(sku, "Product sku cannot be null");
        ProductPrice productPrice = productPriceService.findById(priceId, sku, store);
        if (productPrice == null) {
            throw new ServiceRuntimeException(
                    "An exception occured while getting product price ["
                            + priceId
                            + "] for product sku ["
                            + sku
                            + "] and Store ["
                            + store.getCode()
                            + "]");
        }

        try {
            productPriceService.delete(productPrice);
        } catch (ServiceException e) {
            throw new ServiceRuntimeException(
                    "An exception occured while deleting product price ["
                            + priceId
                            + "] for product sku ["
                            + sku
                            + "] and Store ["
                            + store.getCode()
                            + "]",
                    e);
        }
    }

    private ReadableProductPrice readablePrice(
            ProductPrice price, MerchantStore store, Language language) throws ConversionException {
        ReadableProductPricePopulator populator = new ReadableProductPricePopulator();
        populator.setPricingService(pricingService);
        return populator.populate(price, store, language);
    }

    @Override
    public ReadableProductPrice get(
            String sku, Long productPriceId, MerchantStore store, Language language) {
        Assert.notNull(productPriceId, "Product Price id cannot be null");
        Assert.notNull(store, "MerchantStore cannot be null");
        Assert.notNull(sku, "Product sku cannot be null");
        ProductPrice price = productPriceService.findById(productPriceId, sku, store);

        if (price == null) {
            throw new ResourceNotFoundException(
                    "ProductPrice with id ["
                            + productPriceId
                            + " not found for product sku ["
                            + sku
                            + "] and Store ["
                            + store.getCode()
                            + "]");
        }

        try {
            return readablePrice(price, store, language);
        } catch (ConversionException e) {
            throw new ServiceRuntimeException(
                    "An exception occured while deleting product price ["
                            + productPriceId
                            + "] for product sku ["
                            + sku
                            + "] and Store ["
                            + store.getCode()
                            + "]",
                    e);
        }
    }
}
