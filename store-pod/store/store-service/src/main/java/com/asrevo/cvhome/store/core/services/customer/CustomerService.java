package com.asrevo.cvhome.store.core.services.customer;


import com.asrevo.cvhome.store.core.entity.common.Address;
import com.asrevo.cvhome.store.core.entity.customer.Customer;
import com.asrevo.cvhome.store.core.entity.customer.CustomerCriteria;
import com.asrevo.cvhome.store.core.entity.customer.CustomerList;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;

import java.util.List;


public interface CustomerService extends SalesManagerEntityService<Long, Customer> {

    List<Customer> getByName(String firstName);

    List<Customer> getListByStore(MerchantStore store);

    Customer getByNick(String nick);

    void saveOrUpdate(Customer customer) throws ServiceException;

    CustomerList getListByStore(MerchantStore store, CustomerCriteria criteria);

    Customer getByNick(String nick, int storeId);

    Customer getByNick(String nick, String code);

    /**
     * Password reset token
     *
     */
    Customer getByPasswordResetToken(String storeCode, String token);

    /**
     * Return an {@link Address} object from the client IP address. Uses underlying GeoLocation module
     *
     */
    Address getCustomerAddress(MerchantStore store, String ipAddress)
            throws ServiceException;


}
