/**
 *
 */
package com.asrevo.cvhome.order.service.facade.cart;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.order.entity.shoppingcart.ShoppingCart;
import com.asrevo.cvhome.order.model.shoppingcart.PersistableShoppingCartItem;
import com.asrevo.cvhome.order.model.shoppingcart.ReadableShoppingCart;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import org.springframework.lang.Nullable;

/**
 * </p>Shopping cart Facade which provide abstraction layer between
 * SM core module and Controller.
 * Only Data Object will be exposed to controller by hiding model
 * object from view.</p>
 *
 * @author Umesh Awasthi
 * @author Carl Samson
 * @version 1.0
 * @since1.0
 */
public interface ShoppingCartFacade {

    void saveOrUpdateShoppingCart(ShoppingCart cart) throws Exception;

    /**
     * Modify an item to an existing cart, quantity of line item will reflect item.getQuantity
     *
     * @param cartCode
     * @param item
     * @param store
     * @param language
     * @return
     * @throws Exception
     */
    ReadableShoppingCart modifyCart(
            String cartCode,
            PersistableShoppingCartItem item,
            StoreMerchantId store,
            LanguageCode language)
            throws Exception;

    /**
     * Add item to shopping cart
     *
     * @param item
     * @param store
     * @param language
     */
    ReadableShoppingCart addToCart(
            PersistableShoppingCartItem item, StoreMerchantId store, LanguageCode language);

    /**
     * Removes a shopping cart item
     *
     * @param cartCode
     * @param sku
     * @param merchant
     * @param language
     * @param returnCart
     * @return ReadableShoppingCart or NULL
     * @throws Exception
     */
    @Nullable ReadableShoppingCart removeShoppingCartItem(
            String cartCode,
            String sku,
            StoreMerchantId merchant,
            LanguageCode language,
            boolean returnCart)
            throws Exception;

    /**
     * Retrieves a shopping cart
     *
     * @param code
     * @param store
     * @param language
     * @return
     * @throws Exception
     */
    ReadableShoppingCart getByCode(String code, StoreMerchantId store, LanguageCode language)
            throws Exception;
}
