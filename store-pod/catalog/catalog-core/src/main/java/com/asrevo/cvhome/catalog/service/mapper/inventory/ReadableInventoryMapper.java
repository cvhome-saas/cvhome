package com.asrevo.cvhome.catalog.service.mapper.inventory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.asrevo.cvhome.catalog.entity.product.availability.ProductAvailability;
import com.asrevo.cvhome.catalog.entity.product.price.ProductPrice;
import com.asrevo.cvhome.catalog.model.product.ReadableProductPrice;
import com.asrevo.cvhome.catalog.model.product.inventory.ReadableInventory;
import com.asrevo.cvhome.catalog.model.product.product.price.FinalPrice;
import com.asrevo.cvhome.catalog.service.populator.catalog.ReadableProductPricePopulator;
import com.asrevo.cvhome.catalog.services.pricing.PricingService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.store.controller.exception.ConversionRuntimeException;
import com.asrevo.cvhome.store.core.exception.ConversionException;
import com.asrevo.cvhome.store.core.mapper.Mapper;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;

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
    public ReadableInventory convert(ProductAvailability source, StoreMerchantId store, LanguageCode language) {
        ReadableInventory availability = new ReadableInventory();
        return merge(source, availability, store, language);
    }

    @Override
    public ReadableInventory merge(ProductAvailability source, ReadableInventory destination, StoreMerchantId store,
                                   LanguageCode language) {
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
            if (source.getAvailable() != null) {
                if (source.getProductDateAvailable() != null) {
                    boolean isAfter = LocalDate.now()
                            .isAfter(source.getProductDateAvailable());
                    if (isAfter && source.getAvailable()) {
                        destination.setAvailable(true);
                    }
                    destination.setDateAvailable(source.getProductDateAvailable());
                } else {
                    destination.setAvailable(source.getAvailable());
                }
            }

            if (source.getAuditSection() != null) {
                if (source.getAuditSection().getDateCreated() != null) {
                    destination.setCreationDate(source.getAuditSection().getDateCreated());
                }
            }

            List<ReadableProductPrice> prices = prices(source, store, language);
            destination.setPrices(prices);

            if (!StringUtils.isEmpty(source.getSku())) {
                destination.setSku(source.getSku());
            } else {
                destination.setSku(source.getProduct().getSku());
            }

            FinalPrice price;
            price = pricingService.calculateProductPrice(source);
            destination.setPrice(price.getStringPrice());

        } catch (Exception e) {
            throw new ConversionRuntimeException("Error while converting Inventory", e);
        }

        return destination;
    }

    private List<ReadableProductPrice> prices(ProductAvailability source, StoreMerchantId store, LanguageCode language)
            throws ConversionException {

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
