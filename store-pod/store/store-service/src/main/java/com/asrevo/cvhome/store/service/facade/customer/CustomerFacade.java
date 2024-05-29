/**
 *
 */
package com.asrevo.cvhome.store.service.facade.customer;


import com.asrevo.cvhome.store.core.entity.customer.Customer;
import com.asrevo.cvhome.store.core.entity.customer.CustomerCriteria;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.entity.shoppingcart.ShoppingCart;
import com.asrevo.cvhome.store.core.model.customer.*;
import com.asrevo.cvhome.store.core.model.customer.address.Address;
import com.asrevo.cvhome.store.core.model.customer.optin.PersistableCustomerOptin;

import java.util.List;

/**
 * <p>Customer facade working as a bridge between {@link com.asrevo.cvhome.store.core.services.customer.CustomerService} and Controller
 * It will take care about interacting with Service API and doing any pre and post processing
 * </p>
 *
 * @author Umesh Awasthi
 * @author carlsamson
 * @version 2.2.1 - 2.7.0
 */
public interface CustomerFacade {

    /**
     * Method used to fetch customer based on the username and storecode.
     * Customer username is unique to each store.
     *
     */
    CustomerEntity getCustomerDataByUserName(final String userName, final MerchantStore store, final Language language) throws Exception;

    /**
     * Creates a ReadableCustomer
     *
     */
    ReadableCustomer getCustomerById(final Long id, final MerchantStore merchantStore, final Language language);

    /**
     * Get Customer using unique username
     *
     */
    ReadableCustomer getByUserName(String userName, MerchantStore merchantStore, Language language);

    /**
     * <p>Method responsible for merging cart during authentication,
     * Method will perform following operations
     * <li> Merge Customer Shopping Cart with Session Cart if any.</li>
     * <li> Convert Customer to {@link CustomerEntity} </li>
     * </p>
     *
     * @param customer              username of Customer
     * @param sessionShoppingCartId session shopping cart, if user already have few items in Cart.
     */
    ShoppingCart mergeCart(final Customer customer, final String sessionShoppingCartId, final MerchantStore store, final Language language) throws Exception;

    Customer getCustomerByUserName(final String userName, final MerchantStore store);

    boolean checkIfUserExists(final String userName, final MerchantStore store) throws Exception;

    PersistableCustomer registerCustomer(final PersistableCustomer customer, final MerchantStore merchantStore, final Language language) throws Exception;

    Address getAddress(final Long userId, final MerchantStore merchantStore, boolean isBillingAddress) throws Exception;

    void updateAddress(Long userId, MerchantStore merchantStore, Address address, final Language language)
            throws Exception;

    void setCustomerModelDefaultProperties(Customer customer, MerchantStore store) throws Exception;


    Customer getCustomerModel(PersistableCustomer customer,
                              MerchantStore merchantStore, Language language) throws Exception;

    Customer populateCustomerModel(Customer customerModel, PersistableCustomer customer,
                                   MerchantStore merchantStore, Language language) throws Exception;

    /*
     * Creates a Customer from a PersistableCustomer received from REST API
     */
    ReadableCustomer create(PersistableCustomer customer, MerchantStore store, Language language);


    /**
     * Updates a Customer
     *
     */
    PersistableCustomer update(PersistableCustomer customer, MerchantStore store);

    PersistableCustomer update(String userName, PersistableCustomer customer, MerchantStore store);

    /**
     * Updates only shipping and billing addresses
     *
     */
    void updateAddress(PersistableCustomer customer, MerchantStore store);

    void updateAddress(String userName, PersistableCustomer customer, MerchantStore store);

    /**
     * Save or update a CustomerReview
     *
     */
    PersistableCustomerReview saveOrUpdateCustomerReview(PersistableCustomerReview review, MerchantStore store, Language language);

    /**
     * List all customer reviews by reviewed
     *
     */
    List<ReadableCustomerReview> getAllCustomerReviewsByReviewed(Long customerId, MerchantStore store, Language language);

    /**
     * Deletes a customer review
     *
     */
    void deleteCustomerReview(Long customerId, Long reviewId, MerchantStore store, Language language);

    /**
     * Optin a customer to newsletter
     *
     */
    void optinCustomer(PersistableCustomerOptin optin, MerchantStore store);

    ReadableCustomer getCustomerByNick(String userName, MerchantStore merchantStore, Language language);

    void deleteByNick(String nick);

    void deleteById(Long id);

    void delete(Customer entity);

    ReadableCustomerList getListByStore(MerchantStore store, CustomerCriteria criteria, Language language);

    PersistableCustomerReview createCustomerReview(
            Long customerId,
            PersistableCustomerReview review,
            MerchantStore merchantStore,
            Language language);

    PersistableCustomerReview updateCustomerReview(Long id, Long reviewId, PersistableCustomerReview review, MerchantStore store, Language language);


}
