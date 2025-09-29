package com.asrevo.cvhome.order.services.shoppingcart;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.order.entity.shoppingcart.ShoppingCart;
import com.asrevo.cvhome.order.entity.shoppingcart.ShoppingCartItem;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;
import java.math.BigDecimal;

public interface ShoppingCartService extends SalesManagerEntityService<Long, ShoppingCart> {

    void saveOrUpdate(ShoppingCart shoppingCart) throws ServiceException;

    ShoppingCart getById(Long id, StoreMerchantId store) throws ServiceException;

    ShoppingCart findCart(String code, StoreMerchantId store) throws ServiceException;

    ShoppingCart loadCartByCode(String code, StoreMerchantId store) throws ServiceException;

    /**
     * Populates a ShoppingCartItem from a Product and attributes if any. Calculate price based on availability
     */
    ShoppingCartItem populateShoppingCartItem(String sku, BigDecimal price, StoreMerchantId store)
            throws ServiceException;

    /**
     * Removes a shopping cart item
     */
    void deleteShoppingCartItem(Long id);
}
