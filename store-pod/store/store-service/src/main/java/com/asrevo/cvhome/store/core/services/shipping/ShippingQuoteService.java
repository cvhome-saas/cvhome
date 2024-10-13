package com.asrevo.cvhome.store.core.services.shipping;

import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.shipping.Quote;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.shipping.ShippingSummary;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;

/**
 * Saves and retrieves various shipping quotes done by the system
 *
 * @author c.samson
 */
public interface ShippingQuoteService extends SalesManagerEntityService<Long, Quote> {

    /**
     * Each quote asked for a given shopping cart creates individual Quote object
     * in the table ShippingQuote. This method allows the creation of a ShippingSummary
     * object to work with in the services and in the api.
     *
     */
    ShippingSummary getShippingSummary(Long quoteId, MerchantStore store) throws ServiceException;
}
