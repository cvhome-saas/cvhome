package com.asrevo.cvhome.store.core.services.customer.optin;

import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.system.optin.CustomerOptin;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;

/**
 * Used for optin in customers
 * An implementation example is for signin in users
 *
 * @author carlsamson
 */
public interface CustomerOptinService extends SalesManagerEntityService<Long, CustomerOptin> {

    /**
     * Optin a given customer. This has no reference to a specific Customer object but contains
     * only email, first name and lastname
     *
     */
    void optinCumtomer(CustomerOptin optin) throws ServiceException;

    /**
     * Removes a specific CustomerOptin
     *
     */
    void optoutCumtomer(CustomerOptin optin) throws ServiceException;

    /**
     * Find an existing CustomerOptin
     *
     */
    CustomerOptin findByEmailAddress(MerchantStore store, String emailAddress, String code)
            throws ServiceException;
}
