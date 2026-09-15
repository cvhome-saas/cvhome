package com.asrevo.cvhome.cache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import com.asrevo.cvhome.commons.domain.KeyPart;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.ProductId;
import com.asrevo.cvhome.commons.domain.Sku;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.commons.domain.VariantId;

/**
 * The key of a cached read: the store it belongs to, the language it is rendered in, then the typed parts that
 * name the read.
 *
 * <p>
 * The store comes first and apart from the rest so a store's entries can be dropped together when the store
 * writes, and the parts are typed so a key says what it identifies: a {@link KeyPart} (a sku, a product id, a
 * query hash), a short string (a slug, a code), a small integer or an enum. A raw {@code Long}, a shopper's id, a
 * mutable criteria object or anything else is refused with the type named, at the call, never at read time.
 * {@link #render()} is the key as one string, which is how a remote provider stores it.
 * </p>
 *
 * @param store    the store, or {@link #GLOBAL} for a region whose reads are the same for every tenant
 * @param language the language, or {@code null} for a read that has none (stock and price)
 * @param parts    the rest, in order; may be empty
 */
public record CacheKey(StoreMerchantId store, LanguageCode language, List<Object> parts) implements Serializable {

    /** The store of a global region's keys: the sentinel the security layer already uses for "every store". */
    public static final StoreMerchantId GLOBAL = new StoreMerchantId("*");

    private static final String SEPARATOR = "|";

    private static final String NONE = "-";

    public CacheKey {
        if (store == null) {
            throw new IllegalArgumentException("a cache key names its store");
        }
        parts = List.copyOf(check(parts));
    }

    public static CacheKey of(StoreMerchantId store) {
        return new CacheKey(store, null, List.of());
    }

    public static CacheKey of(StoreMerchantId store, LanguageCode language) {
        return new CacheKey(store, language, List.of());
    }

    /** A read of one sku that has no language: stock and price. */
    public static CacheKey sku(StoreMerchantId store, Sku sku) {
        return new CacheKey(store, null, List.of(sku));
    }

    public static CacheKey sku(StoreMerchantId store, LanguageCode language, Sku sku) {
        return new CacheKey(store, language, List.of(sku));
    }

    public static CacheKey product(StoreMerchantId store, LanguageCode language, ProductId product) {
        return new CacheKey(store, language, List.of(product));
    }

    public static CacheKey variant(StoreMerchantId store, LanguageCode language, VariantId variant) {
        return new CacheKey(store, language, List.of(variant));
    }

    /** A read by a friendly url or a code. */
    public static CacheKey slug(StoreMerchantId store, LanguageCode language, String slug) {
        return new CacheKey(store, language, List.of(slug));
    }

    /** A read by a listing or search criteria, hashed once it is normalised. */
    public static CacheKey query(StoreMerchantId store, LanguageCode language, QueryHash query) {
        return new CacheKey(store, language, List.of(query));
    }

    /** A key of a global region: the same answer for every store. */
    public static CacheKey global(LanguageCode language) {
        return new CacheKey(GLOBAL, language, List.of());
    }

    /** This key with more parts after its own: a page, a limit, a kind. */
    public CacheKey with(Object... more) {
        List<Object> all = new ArrayList<>(parts);
        for (Object part : more) {
            all.add(part);
        }
        return new CacheKey(store, language, all);
    }

    public boolean isGlobal() {
        return GLOBAL.equals(store);
    }

    /** {@code store|language|part|part}: the key as one string. */
    public String render() {
        StringJoiner joiner = new StringJoiner(SEPARATOR);
        joiner.add(store.getId());
        joiner.add(language == null ? NONE : language.code());
        for (Object part : parts) {
            joiner.add(part instanceof KeyPart typed ? typed.cacheKeyPart() : String.valueOf(part));
        }
        return joiner.toString();
    }

    @Override
    public String toString() {
        return render();
    }

    private static List<Object> check(List<Object> parts) {
        if (parts == null) {
            throw new IllegalArgumentException("a cache key's parts are a list, possibly empty");
        }
        for (Object part : parts) {
            if (!allowed(part)) {
                throw new IllegalArgumentException(String.format(
                        "%s cannot be part of a cache key: a part is a KeyPart, a short string, an integer or an enum",
                        part == null ? "null" : part.getClass().getName()));
            }
        }
        return parts;
    }

    private static boolean allowed(Object part) {
        if (part instanceof String text) {
            return !text.isEmpty() && !text.contains(SEPARATOR);
        }
        return part instanceof KeyPart || part instanceof Enum<?> || part instanceof Integer;
    }
}
