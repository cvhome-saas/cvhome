package com.asrevo.cvhome.inventory.service.mapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.inventory.entity.ProductAvailability;
import com.asrevo.cvhome.inventory.entity.ProductPrice;
import com.asrevo.cvhome.inventory.errors.InventoryNotConvertibleException;
import com.asrevo.cvhome.inventory.errors.ProductPriceNotConvertibleException;
import com.asrevo.cvhome.inventory.model.inventory.ReadableInventory;
import com.asrevo.cvhome.inventory.model.price.FinalPriceCalc;
import com.asrevo.cvhome.inventory.model.price.ReadableProductPrice;
import com.asrevo.cvhome.inventory.service.populator.ReadableProductPricePopulator;
import com.asrevo.cvhome.inventory.services.pricing.PricingService;
import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.store.core.mapper.Mapper;

@Component
public class ReadableInventoryMapper implements Mapper<ProductAvailability, ReadableInventory> {

    private final PricingService pricingService;

    private final ExternalMerchantStoreService externalMerchantStoreService;

    public ReadableInventoryMapper(PricingService pricingService,
                                   ExternalMerchantStoreService externalMerchantStoreService) {
        this.pricingService = pricingService;
        this.externalMerchantStoreService = externalMerchantStoreService;
    }

    @Override
    public ReadableInventory convert(ProductAvailability source, StoreMerchantId store, LanguageCode language)
            throws InventoryNotConvertibleException {
        ReadableInventory availability = new ReadableInventory();
        return merge(source, availability, store, language);
    }

    @Override
    public ReadableInventory merge(ProductAvailability source, ReadableInventory destination, StoreMerchantId store,
                                   LanguageCode language) throws InventoryNotConvertibleException {
        try {
            destination.setQuantity(source.getProductQuantity() != null ? source.getProductQuantity() : 0);
            destination.setProductQuantityOrderMax(
                    source.getProductQuantityOrderMax() != null ? source.getProductQuantityOrderMax() : 0);
            destination.setProductQuantityOrderMin(
                    source.getProductQuantityOrderMin() != null ? source.getProductQuantityOrderMin() : 0);
            destination.setOwner(source.getOwner());
            destination.setId(source.getId());
            destination.setRegion(source.getRegion());
            destination.setRegionVariant(source.getRegionVariant());
            destination.setStore(externalMerchantStoreService.getStore(store));
            destination.setSku(source.getSku());
            destination.setProductId(source.getProductId());
            applyAvailability(source, destination);

            if (source.getAuditSection() != null && source.getAuditSection().getDateCreated() != null) {
                destination.setCreationDate(source.getAuditSection().getDateCreated());
            }

            List<ReadableProductPrice> prices = prices(source, store, language);
            destination.setPrices(prices);

            FinalPriceCalc price;
            price = pricingService.calculateProductPrice(source);
            destination.setPrice(price.getStringPrice());

        } catch (Exception e) {
            throw InventoryNotConvertibleException.of(e);
        }

        return destination;
    }

    private void applyAvailability(ProductAvailability source, ReadableInventory destination) {
        if (source.getAvailable() == null) {
            return;
        }
        if (source.getProductDateAvailable() == null) {
            destination.setAvailable(source.getAvailable());
            return;
        }
        boolean isAfter = LocalDate.now().isAfter(source.getProductDateAvailable());
        if (isAfter && source.getAvailable()) {
            destination.setAvailable(true);
        }
        destination.setDateAvailable(source.getProductDateAvailable());
    }

    private List<ReadableProductPrice> prices(ProductAvailability source, StoreMerchantId store, LanguageCode language)
            throws ProductPriceNotConvertibleException {

        ReadableProductPricePopulator populator;
        List<ReadableProductPrice> prices = new ArrayList<>();

        for (ProductPrice price : source.getPrices()) {

            populator = new ReadableProductPricePopulator();
            populator.setPricingService(pricingService);
            ReadableProductPrice p = populator.populate(price, new ReadableProductPrice(), store, language);
            prices.add(p);
        }
        return prices;
    }

}
