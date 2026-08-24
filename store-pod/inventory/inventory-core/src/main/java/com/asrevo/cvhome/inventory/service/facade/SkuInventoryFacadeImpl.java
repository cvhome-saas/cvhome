package com.asrevo.cvhome.inventory.service.facade;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.inventory.entity.ProductAvailability;
import com.asrevo.cvhome.inventory.errors.NoApplicableInventoryException;
import com.asrevo.cvhome.inventory.model.availability.SkuInventory;
import com.asrevo.cvhome.inventory.model.price.FinalPriceCalc;
import com.asrevo.cvhome.inventory.services.availability.ProductAvailabilityService;
import com.asrevo.cvhome.inventory.services.pricing.PricingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SkuInventoryFacadeImpl implements SkuInventoryFacade {

    private final ProductAvailabilityService productAvailabilityService;

    private final PricingService pricingService;

    @Override
    public List<SkuInventory> getBySkus(List<String> skus, StoreMerchantId store, LanguageCode language) {
        List<ProductAvailability> availabilities = productAvailabilityService.getBySkus(skus, store);

        List<SkuInventory> result = new ArrayList<>();
        for (ProductAvailability availability : availabilities) {
            if (!isDefaultAvailability(availability)) {
                continue;
            }
            result.add(toSkuInventory(availability));
        }
        return result;
    }

    /**
     * The single-product model's seam: the one row with no variant and no region variant is the sku's availability.
     */
    private boolean isDefaultAvailability(ProductAvailability availability) {
        return Objects.isNull(availability.getProductVariantId())
                && StringUtils.isEmpty(availability.getRegionVariant());
    }

    private SkuInventory toSkuInventory(ProductAvailability availability) {
        int quantity = Objects.requireNonNullElse(availability.getProductQuantity(), 0);
        boolean available = Boolean.TRUE.equals(availability.getAvailable());
        boolean canBePurchased = available && availability.isProductStatus() && quantity > 0;

        FinalPriceCalc price = null;
        try {
            price = pricingService.calculateProductPrice(availability);
        } catch (NoApplicableInventoryException e) {
            // Not priced yet — the sku is still reported, with no price to show.
            log.debug("Sku {} has no applicable price yet", availability.getSku());
        }

        return SkuInventory.builder()
                .sku(availability.getSku())
                .available(available)
                .canBePurchased(canBePurchased)
                .quantity(quantity)
                .quantityOrderMinimum(Objects.requireNonNullElse(availability.getProductQuantityOrderMin(), 1))
                .quantityOrderMaximum(Objects.requireNonNullElse(availability.getProductQuantityOrderMax(), 0))
                .price(price)
                .build();
    }

}
