/**
 *
 */
package com.asrevo.cvhome.checkout.service.facade.cart;

import com.asrevo.cvhome.checkout.entity.shoppingcart.ShoppingCart;
import com.asrevo.cvhome.checkout.errors.ProductNotPurchasableException;
import com.asrevo.cvhome.checkout.errors.ShoppingCartNotFoundException;
import com.asrevo.cvhome.checkout.model.shoppingcart.PersistableShoppingCartItem;
import com.asrevo.cvhome.checkout.model.shoppingcart.ReadableShoppingCart;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

/**
 * </p>
 * Shopping cart Facade which provide abstraction layer between SM core module and
 * Controller. Only Data Object will be exposed to controller by hiding model object from
 * view.
 * </p>
 *
 * @author Umesh Awasthi
 * @author Carl Samson
 * @version 1.0 @since1.0
 */
public interface ShoppingCartFacade {

    void saveOrUpdateShoppingCart(ShoppingCart cart);

    /**
     * Modify an item in an existing cart; the quantity of the line item reflects {@code item.getQuantity()}.
     *
     * @throws ShoppingCartNotFoundException  no cart with that code in this store
     * @throws ProductNotPurchasableException the product cannot be bought
     */
    ReadableShoppingCart modifyCart(String cartCode, PersistableShoppingCartItem item, StoreMerchantId store,
                                    LanguageCode language)
            throws ShoppingCartNotFoundException, ProductNotPurchasableException;

    /**
     * Add an item to a new shopping cart.
     *
     * @throws ProductNotPurchasableException the product cannot be bought
     */
    ReadableShoppingCart addToCart(PersistableShoppingCartItem item, StoreMerchantId store, LanguageCode language)
            throws ProductNotPurchasableException;

    /**
     * Removes a shopping cart item.
     *
     * @return the remaining cart when {@code returnCart} is set and items remain, otherwise {@code null}
     * @throws ShoppingCartNotFoundException no cart with that code in this store
     */
    ReadableShoppingCart removeShoppingCartItem(String cartCode, String sku, StoreMerchantId merchant,
                                                LanguageCode language, boolean returnCart)
            throws ShoppingCartNotFoundException;

    /**
     * Retrieves a shopping cart, or {@code null} when there is none for that code.
     */
    ReadableShoppingCart getByCode(String code, StoreMerchantId store, LanguageCode language);

}
