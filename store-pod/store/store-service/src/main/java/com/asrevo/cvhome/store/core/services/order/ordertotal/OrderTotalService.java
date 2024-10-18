package com.asrevo.cvhome.store.core.services.order.ordertotal;

import com.asrevo.cvhome.store.core.entity.customer.Customer;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.order.OrderSummary;
import com.asrevo.cvhome.store.core.model.order.OrderTotalVariation;

/**
 * Additional dynamic order total calculation
 * from the rules engine and other modules
 *
 * @author carlsamson
 */
public interface OrderTotalService {

    OrderTotalVariation findOrderTotalVariation(
            final OrderSummary summary,
            final Customer customer,
            final MerchantStore store,
            final Language language)
            throws Exception;
}
