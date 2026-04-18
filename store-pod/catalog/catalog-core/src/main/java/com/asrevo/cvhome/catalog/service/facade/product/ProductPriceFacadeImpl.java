package com.asrevo.cvhome.catalog.service.facade.product;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.asrevo.cvhome.catalog.entity.product.price.ProductPrice;
import com.asrevo.cvhome.catalog.model.product.PersistableProductPrice;
import com.asrevo.cvhome.catalog.model.product.ReadableProductPrice;
import com.asrevo.cvhome.catalog.service.mapper.inventory.PersistableProductPriceMapper;
import com.asrevo.cvhome.catalog.service.populator.catalog.ReadableProductPricePopulator;
import com.asrevo.cvhome.catalog.services.pricing.PricingService;
import com.asrevo.cvhome.catalog.services.product.price.ProductPriceService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.store.controller.exception.ResourceNotFoundException;
import com.asrevo.cvhome.store.controller.exception.ServiceRuntimeException;
import com.asrevo.cvhome.store.core.exception.ConversionException;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;

import static com.asrevo.cvhome.store.utils.NumberUtils.isPositive;

@Service
public class ProductPriceFacadeImpl implements ProductPriceFacade {

    private final ProductPriceService productPriceService;

    private final PricingService pricingService;

    private final PersistableProductPriceMapper persistableProductPriceMapper;

    private final ExternalMerchantStoreService externalMerchantStoreService;

    public ProductPriceFacadeImpl(ProductPriceService productPriceService, PricingService pricingService,
                                  PersistableProductPriceMapper persistableProductPriceMapper,
                                  ExternalMerchantStoreService externalMerchantStoreService) {
        this.productPriceService = productPriceService;
        this.pricingService = pricingService;
        this.persistableProductPriceMapper = persistableProductPriceMapper;
        this.externalMerchantStoreService = externalMerchantStoreService;
    }

    @Override
    public Long save(PersistableProductPrice price, StoreMerchantId store) {
        LanguageCode defaultLanguage = externalMerchantStoreService.getStore(store).getDefaultLanguage();

        ProductPrice productPrice = persistableProductPriceMapper.convert(price, store, defaultLanguage);
        if (!isPositive(productPrice.getId())) {
            // avoid detached entity failed to persist
            productPrice.getProductAvailability().setPrices(null);
        }
        productPrice = productPriceService.saveOrUpdate(productPrice);

        return productPrice.getId();
    }

    @Override
    public List<ReadableProductPrice> list(String sku, Long inventoryId, StoreMerchantId store, LanguageCode language) {
        Assert.notNull(store, "store cannot be null");
        Assert.notNull(sku, "Product sku cannot be null");
        Assert.notNull(inventoryId, "Product inventory cannot be null");

        List<ProductPrice> prices = productPriceService.findByInventoryId(inventoryId, sku, store);

        return prices.stream().map(p -> {
            try {
                return this.readablePrice(p, store, language);
            } catch (ConversionException e) {
                throw new ServiceRuntimeException("An exception occured while getting product price for sku [" + sku
                        + "] and Store [" + store + "]", e);
            }
        }).collect(Collectors.toList());
    }

    @Override
    public List<ReadableProductPrice> list(String sku, StoreMerchantId store, LanguageCode language) {
        Assert.notNull(store, "store cannot be null");
        Assert.notNull(sku, "Product sku cannot be null");

        List<ProductPrice> prices = productPriceService.findByProductSku(sku, store);

        return prices.stream().map(p -> {
            try {
                return this.readablePrice(p, store, language);
            } catch (ConversionException e) {
                throw new ServiceRuntimeException("An exception occured while getting product price for sku [" + sku
                        + "] and Store [" + store + "]", e);
            }
        }).collect(Collectors.toList());
    }

    @Override
    public void delete(Long priceId, String sku, StoreMerchantId store) {
        Assert.notNull(priceId, "Product Price id cannot be null");
        Assert.notNull(store, "store cannot be null");
        Assert.notNull(sku, "Product sku cannot be null");
        ProductPrice productPrice = productPriceService.findById(priceId, sku, store);
        if (productPrice == null) {
            throw new ServiceRuntimeException("An exception occured while getting product price [" + priceId
                    + "] for product sku [" + sku + "] and Store [" + store + "]");
        }

        try {
            productPriceService.delete(productPrice);
        } catch (ServiceException e) {
            throw new ServiceRuntimeException("An exception occured while deleting product price [" + priceId
                    + "] for product sku [" + sku + "] and Store [" + store + "]", e);
        }
    }

    private ReadableProductPrice readablePrice(ProductPrice price, StoreMerchantId store, LanguageCode language)
            throws ConversionException {
        ReadableProductPricePopulator populator = new ReadableProductPricePopulator();
        populator.setPricingService(pricingService);
        return populator.populate(price, store, language);
    }

    @Override
    public ReadableProductPrice get(String sku, Long productPriceId, StoreMerchantId store, LanguageCode language) {
        Assert.notNull(productPriceId, "Product Price id cannot be null");
        Assert.notNull(store, "store cannot be null");
        Assert.notNull(sku, "Product sku cannot be null");
        ProductPrice price = productPriceService.findById(productPriceId, sku, store);

        if (price == null) {
            throw new ResourceNotFoundException("ProductPrice with id [" + productPriceId
                    + " not found for product sku [" + sku + "] and Store [" + store + "]");
        }

        try {
            return readablePrice(price, store, language);
        } catch (ConversionException e) {
            throw new ServiceRuntimeException("An exception occured while deleting product price [" + productPriceId
                    + "] for product sku [" + sku + "] and Store [" + store + "]", e);
        }
    }

}
