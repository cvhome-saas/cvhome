package com.asrevo.cvhome.store.core.services.order;

import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.order.Order;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.entity.shoppingcart.ShoppingCart;
import com.asrevo.cvhome.store.core.model.customer.Customer;
import com.asrevo.cvhome.store.core.model.order.OrderTotalSummary;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;

// @TODO ASHRAF

public interface OrderService extends SalesManagerEntityService<Long, Order> {


    OrderTotalSummary calculateShoppingCartTotal(ShoppingCart cartModel, Customer customer, MerchantStore store, Language language);

    OrderTotalSummary calculateShoppingCartTotal(ShoppingCart cartModel, MerchantStore store, Language language);
}
