package com.asrevo.cvhome.catalog.service.facade.product;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.catalog.entity.product.price.ProductPrice;
import com.asrevo.cvhome.catalog.errors.InventoryReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.errors.ProductPriceNotConvertibleException;
import com.asrevo.cvhome.catalog.errors.ProductPriceNotFoundException;
import com.asrevo.cvhome.catalog.errors.ProductReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.model.product.PersistableProductPrice;
import com.asrevo.cvhome.catalog.model.product.ReadableProductPrice;
import com.asrevo.cvhome.catalog.service.mapper.inventory.PersistableProductPriceMapper;
import com.asrevo.cvhome.catalog.service.populator.catalog.ReadableProductPricePopulator;
import com.asrevo.cvhome.catalog.services.pricing.PricingService;
import com.asrevo.cvhome.catalog.services.product.price.ProductPriceService;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;

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
    public Long save(PersistableProductPrice price, StoreMerchantId store)
            throws ProductPriceNotConvertibleException, InventoryReferenceUnresolvableException,
            ProductReferenceUnresolvableException {
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
    public List<ReadableProductPrice> list(String sku, Long inventoryId, StoreMerchantId store, LanguageCode language)
            throws ProductPriceNotConvertibleException {
        List<ProductPrice> prices = productPriceService.findByInventoryId(inventoryId, sku, store);

        // A plain loop rather than stream().map(...): the price populator declares a checked failure, and the lambda
        // could only swallow it into a message-only runtime wrapper.
        List<ReadableProductPrice> readable = new ArrayList<>();
        for (ProductPrice p : prices) {
            readable.add(this.readablePrice(p, store, language));
        }
        return readable;
    }

    @Override
    public List<ReadableProductPrice> list(String sku, StoreMerchantId store, LanguageCode language)
            throws ProductPriceNotConvertibleException {
        List<ProductPrice> prices = productPriceService.findByProductSku(sku, store);

        // A plain loop rather than stream().map(...): the price populator declares a checked failure, and the lambda
        // could only swallow it into a message-only runtime wrapper.
        List<ReadableProductPrice> readable = new ArrayList<>();
        for (ProductPrice p : prices) {
            readable.add(this.readablePrice(p, store, language));
        }
        return readable;
    }

    @Override
    public void delete(Long priceId, String sku, StoreMerchantId store)
            throws ProductPriceNotFoundException {
        ProductPrice productPrice = productPriceService.findById(priceId, sku, store);
        if (productPrice == null) {
            // Was a ServiceRuntimeException reporting LEGACY.SERVICE_ERROR — a 500 for a price id that simply does
            // not exist.
            throw ProductPriceNotFoundException.of(priceId, store);
        }

        productPriceService.delete(productPrice);
    }

    private ReadableProductPrice readablePrice(ProductPrice price, StoreMerchantId store, LanguageCode language)
            throws ProductPriceNotConvertibleException {
        ReadableProductPricePopulator populator = new ReadableProductPricePopulator();
        populator.setPricingService(pricingService);
        return populator.populate(price, store, language);
    }

    @Override
    public ReadableProductPrice get(String sku, Long productPriceId, StoreMerchantId store, LanguageCode language)
            throws ProductPriceNotFoundException, ProductPriceNotConvertibleException {
        ProductPrice price = productPriceService.findById(productPriceId, sku, store);

        if (price == null) {
            throw ProductPriceNotFoundException.of(productPriceId, store);
        }

        return readablePrice(price, store, language);
    }

}
