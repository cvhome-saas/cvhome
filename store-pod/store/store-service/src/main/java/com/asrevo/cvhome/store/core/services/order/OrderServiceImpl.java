package com.asrevo.cvhome.store.core.services.order;

import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.order.Order;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.entity.shoppingcart.ShoppingCart;
import com.asrevo.cvhome.store.core.model.customer.Customer;
import com.asrevo.cvhome.store.core.model.order.OrderTotalSummary;
import com.asrevo.cvhome.store.core.repositories.order.OrderRepository;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

// @TODO ASHRAF
@Service
public class OrderServiceImpl extends SalesManagerEntityServiceImpl<Long, Order> implements OrderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderServiceImpl.class);


    public OrderServiceImpl(OrderRepository repository) {
        super(repository);
    }

    @Override
    public OrderTotalSummary calculateShoppingCartTotal(ShoppingCart cartModel, Customer customer, MerchantStore store, Language language) {
        return null;
    }

    @Override
    public OrderTotalSummary calculateShoppingCartTotal(ShoppingCart cartModel, MerchantStore store, Language language) {
        return null;
    }
}
