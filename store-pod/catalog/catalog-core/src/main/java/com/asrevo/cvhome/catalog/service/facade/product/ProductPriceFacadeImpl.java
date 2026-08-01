package com.asrevo.cvhome.catalog.service.facade.product;

import java.util.List;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.catalog.entity.product.price.ProductPrice;
import com.asrevo.cvhome.catalog.model.product.PersistableProductPrice;
import com.asrevo.cvhome.catalog.model.product.ReadableProductPrice;
import com.asrevo.cvhome.catalog.service.mapper.inventory.PersistableProductPriceMapper;
import com.asrevo.cvhome.catalog.service.populator.catalog.ReadableProductPricePopulator;
import com.asrevo.cvhome.catalog.services.pricing.PricingService;
import com.asrevo.cvhome.catalog.services.product.price.ProductPriceService;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.store.controller.exception.ResourceNotFoundException;
import com.asrevo.cvhome.store.controller.exception.ServiceRuntimeException;
import com.asrevo.cvhome.store.core.exception.ConversionException;
import com.asrevo.cvhome.store.core.exception.ServiceException;

import static com.asrevo.cvhome.store.utils.NumberUtils.isPositive;

@Service
public class ProductPriceFacadeImpl implements ProductPriceFacade {

    private static final String GET_PRICE_ERROR_MESSAGE = "An exception occured while getting product price for sku [";

    private static final String AND_STORE_MESSAGE = "] and Store [";

    private static final String BRACKET_CLOSE = "]";

    private static final String FOR_PRODUCT_SKU_MESSAGE = "] for product sku [";

    private static final String DELETE_PRICE_ERROR_MESSAGE = "An exception occured while deleting product price [";

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
        List<ProductPrice> prices = productPriceService.findByInventoryId(inventoryId, sku, store);

        return prices.stream().map(p -> {
            try {
                return this.readablePrice(p, store, language);
            } catch (ConversionException e) {
                throw new ServiceRuntimeException(GET_PRICE_ERROR_MESSAGE + sku
                        + AND_STORE_MESSAGE + store + BRACKET_CLOSE, e);
            }
        }).toList();
    }

    @Override
    public List<ReadableProductPrice> list(String sku, StoreMerchantId store, LanguageCode language) {
        List<ProductPrice> prices = productPriceService.findByProductSku(sku, store);

        return prices.stream().map(p -> {
            try {
                return this.readablePrice(p, store, language);
            } catch (ConversionException e) {
                throw new ServiceRuntimeException(GET_PRICE_ERROR_MESSAGE + sku
                        + AND_STORE_MESSAGE + store + BRACKET_CLOSE, e);
            }
        }).toList();
    }

    @Override
    public void delete(Long priceId, String sku, StoreMerchantId store) {
        ProductPrice productPrice = productPriceService.findById(priceId, sku, store);
        if (productPrice == null) {
            throw new ServiceRuntimeException("An exception occured while getting product price [" + priceId
                    + FOR_PRODUCT_SKU_MESSAGE + sku + AND_STORE_MESSAGE + store + BRACKET_CLOSE);
        }

        try {
            productPriceService.delete(productPrice);
        } catch (ServiceException e) {
            throw new ServiceRuntimeException(DELETE_PRICE_ERROR_MESSAGE + priceId
                    + FOR_PRODUCT_SKU_MESSAGE + sku + AND_STORE_MESSAGE + store + BRACKET_CLOSE, e);
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
        ProductPrice price = productPriceService.findById(productPriceId, sku, store);

        if (price == null) {
            throw new ResourceNotFoundException("ProductPrice with id [" + productPriceId
                    + " not found for product sku [" + sku + AND_STORE_MESSAGE + store + BRACKET_CLOSE);
        }

        try {
            return readablePrice(price, store, language);
        } catch (ConversionException e) {
            throw new ServiceRuntimeException(DELETE_PRICE_ERROR_MESSAGE + productPriceId
                    + FOR_PRODUCT_SKU_MESSAGE + sku + AND_STORE_MESSAGE + store + BRACKET_CLOSE, e);
        }
    }

}
