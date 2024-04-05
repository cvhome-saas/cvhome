package com.asrevo.cvhome.store.core.services.order.ordertotal;

import com.asrevo.cvhome.store.core.entity.customer.Customer;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.order.OrderTotal;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.order.OrderSummary;
import com.asrevo.cvhome.store.core.model.order.OrderTotalVariation;
import com.asrevo.cvhome.store.core.model.order.RebatesOrderTotalVariation;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderTotalServiceImpl implements OrderTotalService {


    @Override
    public OrderTotalVariation findOrderTotalVariation(OrderSummary summary, Customer customer, MerchantStore store, Language language)
            throws Exception {

        RebatesOrderTotalVariation variation = new RebatesOrderTotalVariation();

        List<OrderTotal> totals = null;
// @TODO ASHRAF


        return variation;
    }

}
