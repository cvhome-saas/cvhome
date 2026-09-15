package com.asrevo.cvhome.catalog.reads;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.asrevo.cvhome.cache.CacheKey;
import com.asrevo.cvhome.cache.CacheRegistry;
import com.asrevo.cvhome.cache.RegionCache;
import com.asrevo.cvhome.catalog.model.product.ReadableCartLineProduct;
import com.asrevo.cvhome.catalog.model.product.ReadableMinimalProduct;
import com.asrevo.cvhome.catalog.services.product.ProductService;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.Sku;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

/**
 * The two per-sku reads checkout makes on every cart operation, held one sku at a time.
 *
 * <p>
 * A cart's skus are asked for together, but every subset of them is a different call, so a whole-call cache would
 * hit only on the very same cart. Each sku is its own entry, keyed by store, language and sku, and one call is
 * answered from whatever entries exist plus one read for the skus that have none. A sku the catalogue does not know
 * is answered absent and not held: the next call asks again, and a product created a moment later is seen at once.
 * </p>
 */
@Component
public class CartLineReads {

    private final RegionCache<ReadableCartLineProduct> cartLines;

    private final RegionCache<ReadableMinimalProduct> detailed;

    private final ProductService products;

    public CartLineReads(CacheRegistry registry, ProductService products) {
        this.cartLines = registry.region(CatalogRegions.CART_LINE, ReadableCartLineProduct.class);
        this.detailed = registry.region(CatalogRegions.DETAILED_PRODUCT, ReadableMinimalProduct.class);
        this.products = products;
    }

    /** What a cart line renders and nothing more, per sku. */
    public List<ReadableCartLineProduct> cartLines(StoreMerchantId store, LanguageCode language, List<Sku> skus) {
        return read(cartLines, store, language, skus, missing -> products.getCartLines(store, missing, language),
                ReadableCartLineProduct::getSku);
    }

    /** The full product per sku, for the callers that still need it. */
    public List<ReadableMinimalProduct> detailedProducts(StoreMerchantId store, LanguageCode language,
                                                         List<Sku> skus) {
        return read(detailed, store, language, skus, missing -> products.getBySkus(store, missing, language),
                ReadableMinimalProduct::getSku);
    }

    private static <V> List<V> read(RegionCache<V> region, StoreMerchantId store, LanguageCode language,
                                    List<Sku> skus, Function<List<Sku>, List<V>> loader, Function<V, Sku> skuOf) {
        if (skus == null || skus.isEmpty()) {
            return List.of();
        }
        List<Sku> distinct = skus.stream().distinct().toList();
        Map<CacheKey, V> found = region.getAll(distinct.stream().map(sku -> CacheKey.sku(store, language, sku)).toList(),
                missing -> load(store, language, missing, loader, skuOf));
        List<V> lines = new ArrayList<>(distinct.size());
        for (Sku sku : distinct) {
            V line = found.get(CacheKey.sku(store, language, sku));
            if (line != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    private static <V> Map<CacheKey, V> load(StoreMerchantId store, LanguageCode language, Set<CacheKey> keys,
                                             Function<List<Sku>, List<V>> loader, Function<V, Sku> skuOf) {
        List<Sku> skus = keys.stream().map(key -> (Sku) key.parts().getFirst()).toList();
        Map<CacheKey, V> loaded = new LinkedHashMap<>();
        for (V line : loader.apply(skus)) {
            loaded.put(CacheKey.sku(store, language, skuOf.apply(line)), line);
        }
        return loaded;
    }
}
