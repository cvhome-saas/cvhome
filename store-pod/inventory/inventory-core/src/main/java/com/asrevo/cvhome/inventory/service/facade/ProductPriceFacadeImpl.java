package com.asrevo.cvhome.inventory.service.facade;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.inventory.entity.ProductPrice;
import com.asrevo.cvhome.inventory.errors.InventoryReferenceUnresolvableException;
import com.asrevo.cvhome.inventory.errors.ProductPriceNotConvertibleException;
import com.asrevo.cvhome.inventory.errors.ProductPriceNotFoundException;
import com.asrevo.cvhome.inventory.errors.SkuReferenceUnresolvableException;
import com.asrevo.cvhome.inventory.model.price.PersistableProductPrice;
import com.asrevo.cvhome.inventory.model.price.ReadableProductPrice;
import com.asrevo.cvhome.inventory.service.mapper.PersistableProductPriceMapper;
import com.asrevo.cvhome.inventory.service.populator.ReadableProductPricePopulator;
import com.asrevo.cvhome.inventory.services.price.ProductPriceService;
import com.asrevo.cvhome.inventory.services.pricing.PricingService;

import static com.asrevo.cvhome.store.utils.NumberUtils.isPositive;

@Service
public class ProductPriceFacadeImpl implements ProductPriceFacade {

    private final ProductPriceService productPriceService;

    private final PricingService pricingService;

    private final PersistableProductPriceMapper persistableProductPriceMapper;

    public ProductPriceFacadeImpl(ProductPriceService productPriceService, PricingService pricingService,
                                  PersistableProductPriceMapper persistableProductPriceMapper) {
        this.productPriceService = productPriceService;
        this.pricingService = pricingService;
        this.persistableProductPriceMapper = persistableProductPriceMapper;
    }

    @Override
    public Long save(PersistableProductPrice price, StoreMerchantId store)
            throws ProductPriceNotConvertibleException, InventoryReferenceUnresolvableException,
            SkuReferenceUnresolvableException {

        ProductPrice productPrice = persistableProductPriceMapper.convert(price, store, null);
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

        // A plain loop rather than stream().map(...): the price populator declares a checked failure.
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

        // A plain loop rather than stream().map(...): the price populator declares a checked failure.
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
