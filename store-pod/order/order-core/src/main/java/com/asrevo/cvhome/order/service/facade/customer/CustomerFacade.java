/**
 *
 */
package com.asrevo.cvhome.order.service.facade.customer;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.customer.model.customer.PersistableCustomer;
import com.asrevo.cvhome.customer.model.customer.ReadableCustomer;
import com.asrevo.cvhome.customer.model.customer.ReadableCustomerList;
import com.asrevo.cvhome.order.entity.customer.Customer;
import com.asrevo.cvhome.order.entity.customer.CustomerCriteria;
import com.asrevo.cvhome.order.services.customer.CustomerService;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;

/**
 * <p>Customer facade working as a bridge between {@link CustomerService} and Controller
 * It will take care about interacting with Service API and doing any pre and post processing
 * </p>
 *
 * @author Umesh Awasthi
 * @author carlsamson
 * @version 2.2.1 - 2.7.0
 */
public interface CustomerFacade {

    /**
     * Creates a ReadableCustomer
     */
    ReadableCustomer getCustomerById(Long id, StoreMerchantId store, LanguageCode language);

    boolean checkIfUserExists(String userName, StoreMerchantId store);

    Customer populateCustomerModel(
            Customer customerModel,
            PersistableCustomer customer,
            StoreMerchantId store,
            LanguageCode language)
            throws Exception;

    ReadableCustomerList getListByStore(
            StoreMerchantId store, CustomerCriteria criteria, LanguageCode language);
}
