package com.asrevo.cvhome.checkout.service.facade.product;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.catalog.model.product.ReadableMinimalProduct;
import com.asrevo.cvhome.catalog.services.product.ExternalProductService;
import com.asrevo.cvhome.checkout.model.product.ProductDetails;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.inventory.model.availability.ReadableProductAvailability;
import com.asrevo.cvhome.inventory.model.availability.SkuInventory;
import com.asrevo.cvhome.inventory.services.ExternalInventoryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductDetailsComposerImpl implements ProductDetailsComposer {

    private final ExternalProductService externalProductService;

    private final ExternalInventoryService externalInventoryService;

    @Override
    public ProductDetails getDetailedProduct(StoreMerchantId store, String sku, LanguageCode language) {
        ReadableMinimalProduct product = externalProductService.getDetailedProduct(store, sku, language);
        SkuInventory inventory = externalInventoryService.getBySkus(store, List.of(sku), language).stream()
                .filter(it -> Objects.equals(it.sku(), sku))
                .findFirst()
                .orElse(null);

        return new ProductDetails(product,
                inventory == null ? null : inventory.price(),
                toAvailability(sku, store, inventory));
    }

    /**
     * A sku inventory answered nothing for is simply not stocked — quantity zero, not purchasable — never an error:
     * the cart still has to render the line.
     */
    private ReadableProductAvailability toAvailability(String sku, StoreMerchantId store, SkuInventory inventory) {
        ReadableProductAvailability availability = new ReadableProductAvailability();
        availability.setSku(sku);
        availability.setStore(store);
        if (inventory == null) {
            availability.setQuantity(0);
            availability.setCanBePurchased(false);
            return availability;
        }
        availability.setQuantity(inventory.quantity());
        availability.setCanBePurchased(inventory.canBePurchased());
        availability.setQuantityOrderMinimum(inventory.quantityOrderMinimum());
        availability.setQuantityOrderMaximum(inventory.quantityOrderMaximum());
        return availability;
    }

}
