package com.asrevo.cvhome.checkout.services.cart;

import com.asrevo.cvhome.checkout.domain.CartCode;
import com.asrevo.cvhome.checkout.domain.ShopperId;
import com.asrevo.cvhome.checkout.errors.CartAlreadyConvertedException;
import com.asrevo.cvhome.checkout.errors.CartNotFoundException;
import com.asrevo.cvhome.checkout.errors.CartQuantityOutOfRangeException;
import com.asrevo.cvhome.checkout.errors.ProductNotPurchasableException;
import com.asrevo.cvhome.checkout.model.cart.PersistableCartItem;
import com.asrevo.cvhome.checkout.model.cart.ReadableCart;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

/**
 * The shopper's cart. Public, keyed by the cart code the browser holds; every read re-prices the lines from inventory.
 */
public interface CartService {

    ReadableCart create(StoreMerchantId store, LanguageCode language, PersistableCartItem item, ShopperId shopper)
            throws ProductNotPurchasableException, CartQuantityOutOfRangeException;

    /** Sets the line to exactly the given quantity; zero removes it. */
    ReadableCart upsert(StoreMerchantId store, LanguageCode language, CartCode code, PersistableCartItem item)
            throws CartNotFoundException, CartAlreadyConvertedException, ProductNotPurchasableException,
            CartQuantityOutOfRangeException;

    ReadableCart get(StoreMerchantId store, LanguageCode language, CartCode code) throws CartNotFoundException;

    ReadableCart removeLine(StoreMerchantId store, LanguageCode language, CartCode code, String sku)
            throws CartNotFoundException, CartAlreadyConvertedException;
}
