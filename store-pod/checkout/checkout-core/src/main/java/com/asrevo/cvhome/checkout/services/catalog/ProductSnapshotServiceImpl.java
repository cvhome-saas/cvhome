package com.asrevo.cvhome.checkout.services.catalog;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.catalog.model.product.ReadableMinimalProduct;
import com.asrevo.cvhome.catalog.services.product.ExternalProductService;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.inventory.model.AvailabilityQuery;
import com.asrevo.cvhome.inventory.model.SkuInventory;
import com.asrevo.cvhome.inventory.services.ExternalInventoryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductSnapshotServiceImpl implements ProductSnapshotService {

    private final ExternalProductService products;

    private final ExternalInventoryService inventory;

    @Override
    public Map<String, ProductSnapshot> snapshot(StoreMerchantId store, LanguageCode language, Collection<String> skus) {
        if (skus.isEmpty()) {
            return Map.of();
        }
        List<String> distinct = skus.stream().distinct().toList();
        Map<String, ReadableMinimalProduct> byProductSku = products.getDetailedProducts(store, distinct, language)
                .stream().collect(Collectors.toMap(ReadableMinimalProduct::getSku, Function.identity(), (a, b) -> a));
        Map<String, SkuInventory> byStockSku = inventory.queryBySkus(store, new AvailabilityQuery(distinct)).stream()
                .collect(Collectors.toMap(SkuInventory::sku, Function.identity(), (a, b) -> a));

        Map<String, ProductSnapshot> result = new LinkedHashMap<>();
        for (String sku : distinct) {
            ReadableMinimalProduct product = byProductSku.get(sku);
            SkuInventory stock = byStockSku.get(sku);
            if (product == null || stock == null || stock.price() == null) {
                continue;
            }
            BigDecimal finalPrice = stock.price().finalPrice() == null ? BigDecimal.ZERO : stock.price().finalPrice();
            BigDecimal original = stock.price().originalPrice() == null ? finalPrice : stock.price().originalPrice();
            result.put(sku, new ProductSnapshot(sku, product, finalPrice, original, stock.price().discounted(),
                    stock.available() && stock.canBePurchased() && product.isAvailable(),
                    stock.quantityOrderMinimum(), stock.quantityOrderMaximum()));
        }
        return result;
    }
}
