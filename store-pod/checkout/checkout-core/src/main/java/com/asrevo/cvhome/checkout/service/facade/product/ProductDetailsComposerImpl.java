package com.asrevo.cvhome.checkout.service.facade.product;

import java.util.List;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.catalog.model.product.ReadableMinimalProduct;
import com.asrevo.cvhome.catalog.services.product.ExternalProductService;
import com.asrevo.cvhome.checkout.model.product.ProductDetails;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.inventory.model.SkuInventory;
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
        SkuInventory inventory = externalInventoryService.getBySkus(store, List.of(sku)).stream()
                .filter(it -> sku.equals(it.sku()))
                .findFirst()
                .orElseGet(() -> notStocked(sku, product));
        return new ProductDetails(product, inventory);
    }

    /**
     * A sku inventory has no record for is simply not stocked — never an error, the cart still has to render the
     * line.
     */
    private static SkuInventory notStocked(String sku, ReadableMinimalProduct product) {
        return new SkuInventory(sku, product == null ? null : product.getId(), false, false, 0, 1, 0, null);
    }
}
