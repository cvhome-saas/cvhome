package com.asrevo.cvhome.store.core.services.shoppingcart;


import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import com.asrevo.cvhome.store.core.entity.customer.Customer;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.shoppingcart.ShoppingCart;
import com.asrevo.cvhome.store.core.entity.shoppingcart.ShoppingCartItem;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.shipping.ShippingProduct;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;

import java.util.List;

public interface ShoppingCartService extends SalesManagerEntityService<Long, ShoppingCart> {

    ShoppingCart getShoppingCart(Customer customer, MerchantStore store) throws ServiceException;

    void saveOrUpdate(ShoppingCart shoppingCart) throws ServiceException;

    ShoppingCart getById(Long id, MerchantStore store) throws ServiceException;

    ShoppingCart getByCode(String code, MerchantStore store) throws ServiceException;


    /**
     * Creates a list of ShippingProduct based on the ShoppingCart if items are
     * virtual return list will be null
     *
     * @param cart
     * @return
     * @throws ServiceException
     */
    List<ShippingProduct> createShippingProduct(ShoppingCart cart) throws ServiceException;

    /**
     * Populates a ShoppingCartItem from a Product and attributes if any. Calculate price based on availability
     *
     * @param product
     * @return
     * @throws ServiceException
     */
    ShoppingCartItem populateShoppingCartItem(Product product, MerchantStore store) throws ServiceException;

    void deleteCart(ShoppingCart cart) throws ServiceException;

    void removeShoppingCart(ShoppingCart cart) throws ServiceException;

    /**
     * @param userShoppingModel
     * @param sessionCart
     * @param store
     * @return {@link ShoppingCart} merged Shopping Cart
     * @throws Exception
     */
    ShoppingCart mergeShoppingCarts(final ShoppingCart userShoppingCart, final ShoppingCart sessionCart,
                                    final MerchantStore store) throws Exception;


    /**
     * Removes a shopping cart item
     *
     * @param item
     */
    void deleteShoppingCartItem(Long id);

}