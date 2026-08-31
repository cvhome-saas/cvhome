package com.asrevo.cvhome.checkout.service.facade.product;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

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

    @Override
    public Map<String, ProductDetails> getDetailedProducts(StoreMerchantId store, Collection<String> skus,
                                                           LanguageCode language) {
        if (skus == null || skus.isEmpty()) {
            return Map.of();
        }
        List<String> distinct = List.copyOf(new LinkedHashSet<>(skus));
        Map<String, ReadableMinimalProduct> products = new LinkedHashMap<>();
        for (ReadableMinimalProduct product : externalProductService.getDetailedProducts(store, distinct,
                language)) {
            products.putIfAbsent(product.getSku(), product);
        }
        Map<String, SkuInventory> inventories = new LinkedHashMap<>();
        for (SkuInventory inventory : externalInventoryService.getBySkus(store, distinct)) {
            inventories.putIfAbsent(inventory.sku(), inventory);
        }
        Map<String, ProductDetails> details = new LinkedHashMap<>();
        for (String sku : distinct) {
            ReadableMinimalProduct product = products.get(sku);
            if (product == null) {
                continue; // gone from the catalogue — the caller degrades the line, the map stays honest
            }
            details.put(sku, new ProductDetails(product,
                    inventories.getOrDefault(sku, notStocked(sku, product))));
        }
        return details;
    }

    /**
     * A sku inventory has no record for is simply not stocked — never an error, the cart still has to render the
     * line.
     */
    private static SkuInventory notStocked(String sku, ReadableMinimalProduct product) {
        return new SkuInventory(sku, product == null ? null : product.getId(), false, false, 0, 1, 0, null);
    }
}
