package com.asrevo.cvhome.checkout.services.catalog;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.catalog.model.product.ReadableCartLineProduct;
import com.asrevo.cvhome.catalog.model.product.ReadableVariantOptionValue;
import com.asrevo.cvhome.catalog.model.product.ReadableVariantSelection;
import com.asrevo.cvhome.catalog.services.product.ExternalProductService;
import com.asrevo.cvhome.checkout.entity.CartLine;
import com.asrevo.cvhome.checkout.entity.OptionLabel;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.Sku;
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

    private final Clock clock;

    @Override
    public Map<Sku, ProductSnapshot> snapshot(StoreMerchantId store, LanguageCode language, Collection<Sku> skus) {
        if (skus.isEmpty()) {
            return Map.of();
        }
        List<Sku> distinct = skus.stream().distinct().toList();
        return merge(distinct, fromCatalog(store, language, distinct), fromInventory(store, distinct));
    }

    @Override
    public Map<Sku, ProductSnapshot> priced(StoreMerchantId store, LanguageCode language, Collection<CartLine> lines) {
        if (lines.isEmpty()) {
            return Map.of();
        }
        Instant now = clock.instant();
        List<Sku> skus = lines.stream().map(CartLine::getSku).distinct().toList();
        Map<Sku, ReadableCartLineProduct> byProductSku = new LinkedHashMap<>();
        List<Sku> unknown = new ArrayList<>();
        for (CartLine line : lines) {
            if (line.remembers(now)) {
                byProductSku.put(line.getSku(), remembered(line));
            } else {
                unknown.add(line.getSku());
            }
        }
        if (!unknown.isEmpty()) {
            Map<Sku, ReadableCartLineProduct> fresh = fromCatalog(store, language, unknown);
            for (CartLine line : lines) {
                ReadableCartLineProduct product = fresh.get(line.getSku());
                if (product != null) {
                    remember(line, product, now);
                    byProductSku.put(line.getSku(), product);
                }
            }
        }
        return merge(skus, byProductSku, fromInventory(store, skus));
    }

    /** Keeps what the catalogue answered on the line, so the next read need not ask again. */
    public static void remember(CartLine line, ReadableCartLineProduct product, Instant now) {
        line.remember(product.getProductId(), product.getName(), product.getFriendlyUrl(), product.getImageUrl(),
                product.isAvailable(), labelsOf(product), now);
    }

    private Map<Sku, ReadableCartLineProduct> fromCatalog(StoreMerchantId store, LanguageCode language,
                                                          List<Sku> skus) {
        return products.getCartLines(store, skus, language).stream()
                .collect(Collectors.toMap(ReadableCartLineProduct::getSku, Function.identity(), (a, b) -> a,
                        LinkedHashMap::new));
    }

    private Map<Sku, SkuInventory> fromInventory(StoreMerchantId store, List<Sku> skus) {
        return inventory.queryBySkus(store, new AvailabilityQuery(skus)).stream()
                .collect(Collectors.toMap(SkuInventory::sku, Function.identity(), (a, b) -> a));
    }

    private static Map<Sku, ProductSnapshot> merge(List<Sku> skus, Map<Sku, ReadableCartLineProduct> byProductSku,
                                                   Map<Sku, SkuInventory> byStockSku) {
        Map<Sku, ProductSnapshot> result = new LinkedHashMap<>();
        for (Sku sku : skus) {
            ReadableCartLineProduct product = byProductSku.get(sku);
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

    /** The line's snapshot in the catalog's shape, so a read and an add are priced and mapped the same way. */
    private static ReadableCartLineProduct remembered(CartLine line) {
        ReadableCartLineProduct product = new ReadableCartLineProduct();
        product.setSku(line.getSku());
        product.setProductId(line.getProductId());
        product.setName(line.getProductName());
        product.setFriendlyUrl(line.getFriendlyUrl());
        product.setImageUrl(line.getImageUrl());
        product.setAvailable(Boolean.TRUE.equals(line.getCatalogAvailable()));
        if (!line.getOptionLabels().isEmpty()) {
            ReadableVariantSelection selection = new ReadableVariantSelection();
            selection.setSku(line.getSku());
            selection.setOptionValues(line.getOptionLabels().stream().map(label -> {
                ReadableVariantOptionValue value = new ReadableVariantOptionValue();
                value.setOptionName(label.option());
                value.setValueName(label.value());
                return value;
            }).toList());
            product.setVariant(selection);
        }
        return product;
    }

    private static List<OptionLabel> labelsOf(ReadableCartLineProduct product) {
        if (product.getVariant() == null || product.getVariant().getOptionValues() == null) {
            return List.of();
        }
        return product.getVariant().getOptionValues().stream()
                .map(value -> new OptionLabel(
                        value.getOptionName() == null ? value.getOptionCode() : value.getOptionName(),
                        value.getValueName() == null ? value.getValueCode() : value.getValueName()))
                .toList();
    }
}
