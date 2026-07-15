package com.asrevo.cvhome.checkout.services.shoppingcart;

import java.math.BigDecimal;

import com.asrevo.cvhome.checkout.entity.shoppingcart.ShoppingCart;
import com.asrevo.cvhome.checkout.entity.shoppingcart.ShoppingCartItem;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;

public interface ShoppingCartService extends SalesManagerEntityService<Long, ShoppingCart> {

    void saveOrUpdate(ShoppingCart shoppingCart) throws ServiceException;

    ShoppingCart findCart(Long id, StoreMerchantId store);

    ShoppingCart findCart(String code, StoreMerchantId store);

    ShoppingCart loadCartByCode(String code, StoreMerchantId store, LanguageCode languageCode) throws ServiceException;

    /**
     * Populates a ShoppingCartItem from a Product and attributes if any. Calculate price
     * based on availability
     */
    ShoppingCartItem populateShoppingCartItem(String sku, BigDecimal price, StoreMerchantId store)
            throws ServiceException;

    /**
     * Removes a shopping cart item
     */
    void deleteShoppingCartItem(Long id);

}
