/**
 *
 */
package com.asrevo.cvhome.store.service.facade.customer;

import com.asrevo.cvhome.store.controller.exception.*;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.entity.customer.Customer;
import com.asrevo.cvhome.store.core.entity.customer.CustomerCriteria;
import com.asrevo.cvhome.store.core.entity.customer.CustomerList;
import com.asrevo.cvhome.store.core.entity.customer.review.CustomerReview;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.country.Country;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.entity.reference.zone.Zone;
import com.asrevo.cvhome.store.core.entity.shoppingcart.ShoppingCart;
import com.asrevo.cvhome.store.core.entity.system.optin.CustomerOptin;
import com.asrevo.cvhome.store.core.entity.system.optin.Optin;
import com.asrevo.cvhome.store.core.entity.system.optin.OptinType;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.customer.ReadableCustomerList;
import com.asrevo.cvhome.store.core.model.customer.*;
import com.asrevo.cvhome.store.core.model.customer.address.Address;
import com.asrevo.cvhome.store.core.model.customer.optin.PersistableCustomerOptin;
import com.asrevo.cvhome.store.core.services.customer.CustomerService;
import com.asrevo.cvhome.store.core.services.customer.optin.CustomerOptinService;
import com.asrevo.cvhome.store.core.services.customer.review.CustomerReviewService;
import com.asrevo.cvhome.store.core.services.reference.country.CountryService;
import com.asrevo.cvhome.store.core.services.reference.language.LanguageService;
import com.asrevo.cvhome.store.core.services.reference.zone.ZoneService;
import com.asrevo.cvhome.store.core.services.shoppingcart.ShoppingCartService;
import com.asrevo.cvhome.store.core.services.system.optin.OptinService;
import com.asrevo.cvhome.store.service.populator.customer.*;
import com.asrevo.cvhome.store.utils.CoreConfiguration;
import com.asrevo.cvhome.store.utils.LocaleUtils;
import com.asrevo.cvhome.store.utils.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Customer Facade work as an abstraction layer between Controller and Service layer. It work as an
 * entry point to service layer.
 *
 * @author Umesh Awasthi
 * @version 2.2.1, 2.8.0
 * @modified Carl Samson
 */
// @TODO ASHRAF

@Service("customerFacade")
@Slf4j
public class CustomerFacadeImpl implements CustomerFacade {


    private final CustomerService customerService;

    private final OptinService optinService;

    private final CustomerOptinService customerOptinService;

    private final ShoppingCartService shoppingCartService;

    private final LanguageService languageService;


    private final CountryService countryService;


    private final ZoneService zoneService;


    private final CustomerReviewService customerReviewService;

    private final CoreConfiguration coreConfiguration;

    private final CustomerPopulator customerPopulator;

    public CustomerFacadeImpl(CustomerService customerService, OptinService optinService, CustomerOptinService customerOptinService, ShoppingCartService shoppingCartService, LanguageService languageService, CountryService countryService, ZoneService zoneService, CustomerReviewService customerReviewService, CoreConfiguration coreConfiguration, CustomerPopulator customerPopulator) {
        this.customerService = customerService;
        this.optinService = optinService;
        this.customerOptinService = customerOptinService;
        this.shoppingCartService = shoppingCartService;
        this.languageService = languageService;
        this.countryService = countryService;
        this.zoneService = zoneService;
        this.customerReviewService = customerReviewService;
        this.coreConfiguration = coreConfiguration;
        this.customerPopulator = customerPopulator;
    }


    /**
     * Method used to fetch customer based on the username and storecode. Customer username is unique
     * to each store.generateRandomBytes
     *
     * @param userName
     * @param store
     * @throws ConversionException
     */
    @Override
    public CustomerEntity getCustomerDataByUserName(final String userName, final MerchantStore store,
                                                    final Language language) throws Exception {
        log.info("Fetching customer with userName" + userName);
        com.asrevo.cvhome.store.core.entity.customer.Customer customer = customerService.getByNick(userName);

        if (customer != null) {
            log.info("Found customer, converting to CustomerEntity");
            try {
                CustomerEntityPopulator customerEntityPopulator = new CustomerEntityPopulator();
                return customerEntityPopulator.populate(customer, store, language); // store, language

            } catch (ConversionException ex) {
                log.error("Error while converting Customer to CustomerEntity", ex);
                throw new Exception(ex);
            }
        }

        return null;

    }


    /*
     * (non-Javadoc)
     *
     * @see com.salesmanager.web.shop.controller.customer.facade#mergeCart(final Customer
     * customerModel, final String sessionShoppingCartId ,final MerchantStore store,final Language
     * language)
     */
    @Override
    public ShoppingCart mergeCart(final Customer customerModel, final String sessionShoppingCartId,
                                  final MerchantStore store, final Language language) throws Exception {

        log.debug("Starting merge cart process");
        if (customerModel != null) {
            ShoppingCart customerCart = shoppingCartService.getShoppingCart(customerModel, store);
            if (StringUtils.isNotBlank(sessionShoppingCartId)) {
                ShoppingCart sessionShoppingCart =
                        shoppingCartService.getByCode(sessionShoppingCartId, store);
                if (sessionShoppingCart != null) {
                    if (customerCart == null) {
                        if (sessionShoppingCart.getCustomerId() == null) {// saved shopping cart does not belong
                            // to a customer
                            log.debug("Not able to find any shoppingCart with current customer");
                            // give it to the customer
                            sessionShoppingCart.setCustomerId(customerModel.getId());
                            shoppingCartService.saveOrUpdate(sessionShoppingCart);
                            customerCart = shoppingCartService.getById(sessionShoppingCart.getId(), store);
                            return customerCart;
                            // return populateShoppingCartData(customerCart,store,language);
                        } else {
                            return null;
                        }
                    } else {
                        if (sessionShoppingCart.getCustomerId() == null) {// saved shopping cart does not belong
                            // to a customer
                            // assign it to logged in user
                            log.debug("Customer shopping cart as well session cart is available, merging carts");
                            customerCart =
                                    shoppingCartService.mergeShoppingCarts(customerCart, sessionShoppingCart, store);
                            customerCart = shoppingCartService.getById(customerCart.getId(), store);
                            return customerCart;
                            // return populateShoppingCartData(customerCart,store,language);
                        } else {
                            if (sessionShoppingCart.getCustomerId().longValue() == customerModel.getId()
                                    .longValue()) {
                                if (!customerCart.getShoppingCartCode()
                                        .equals(sessionShoppingCart.getShoppingCartCode())) {
                                    // merge carts
                                    log.info("Customer shopping cart as well session cart is available");
                                    customerCart = shoppingCartService.mergeShoppingCarts(customerCart,
                                            sessionShoppingCart, store);
                                    customerCart = shoppingCartService.getById(customerCart.getId(), store);
                                    return customerCart;
                                    // return populateShoppingCartData(customerCart,store,language);
                                } else {
                                    return customerCart;
                                    // return populateShoppingCartData(sessionShoppingCart,store,language);
                                }
                            } else {
                                // the saved cart belongs to another user
                                return null;
                            }
                        }


                    }
                }
            } else {
                if (customerCart != null) {
                    // return populateShoppingCartData(customerCart,store,language);
                    return customerCart;
                }
                return null;

            }
        }
        log.info(
                "Seems some issue with system, unable to find any customer after successful authentication");
        return null;

    }


    @Override
    //KEEP
    public Customer getCustomerByUserName(String userName, MerchantStore store) {

        try {
            return customerService.getByNick(userName, store.getId());
        } catch (Exception e) {
            throw new ServiceRuntimeException(e);
        }

    }

    @Override
    public ReadableCustomer getByUserName(String userName, MerchantStore merchantStore, Language language) {
        Assert.notNull(userName, "Username cannot be null");
        Assert.notNull(merchantStore, "MerchantStore cannot be null");

        Customer customerModel = getCustomerByNickAndStoreId(userName, merchantStore);
        return convertCustomerToReadableCustomer(customerModel, merchantStore, language);
    }

    private Customer getCustomerByNickAndStoreId(String userName, MerchantStore merchantStore) {
        return Optional.ofNullable(customerService.getByNick(userName, merchantStore.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("No Customer found for ID : " + userName));
    }


    /**
     * <p>
     * Method to check if given user exists for given username under given store. System treat
     * username as unique for a given store, customer is not allowed to use same username twice for a
     * given store, however it can be used for different stores.
     * </p>
     *
     * @param userName Customer slected userName
     * @param store    store for which customer want to register
     * @return boolean flag indicating if user exists for given store or not
     * @throws Exception
     */
    @Override
    public boolean checkIfUserExists(final String userName, final MerchantStore store)
            throws Exception {
        if (StringUtils.isNotBlank(userName) && store != null) {
            Customer customer = customerService.getByNick(userName, store.getId());
            if (customer != null) {
                log.info("Customer with userName {} already exists for store {} ", userName,
                        store.getStorename());
                return true;
            }

            log.info("No customer found with userName {} for store {} ", userName, store.getStorename());
            return false;

        }
        log.info("Either userName is empty or we have not found any value for store");
        return false;
    }


    @Override
    public PersistableCustomer registerCustomer(final PersistableCustomer customer,
                                                final MerchantStore merchantStore, Language language) throws Exception {
        log.info("Starting customer registration process..");

        if (userExist(customer.getUserName())) {
            throw new UserAlreadyExistException("User already exist");
        }

        Customer customerModel = getCustomerModel(customer, merchantStore, language);
        if (customerModel == null) {
            log.equals("Unable to create customer in system");
            // throw new CustomerRegistrationException( "Unable to register customer" );
            throw new Exception("Unable to register customer");
        }

        log.info("About to persist customer to database.");
        customerService.saveOrUpdate(customerModel);

        log.info("Returning customer data to controller..");
        // return customerEntityPoulator(customerModel,merchantStore);
        customer.setId(customerModel.getId());
        return customer;
    }

    @Override
    public Customer getCustomerModel(final PersistableCustomer customer,
                                     final MerchantStore merchantStore, Language language) throws Exception {

        log.info("Starting to populate customer model from customer data");
        Customer customerModel = null;

        customerModel = customerPopulator.populate(customer, merchantStore, language);
        // we are creating or resetting a customer
        if (StringUtils.isBlank(customerModel.getPassword())
                && !StringUtils.isBlank(customer.getPassword())) {
            customerModel.setPassword(customer.getPassword());
        }
        // set groups
        if (!StringUtils.isBlank(customerModel.getPassword())
                && !StringUtils.isBlank(customerModel.getNick())) {
            customerModel.setPassword(/*@TODO ASHRAF*/customer.getPassword()/*passwordEncoder.encode(customer.getPassword())*/);
            setCustomerModelDefaultProperties(customerModel, merchantStore);
        }


        return customerModel;

    }


    @Override
    public void setCustomerModelDefaultProperties(Customer customer, MerchantStore store)
            throws Exception {
        Assert.notNull(customer, "Customer object cannot be null");
        if (customer.getId() == null || customer.getId() == 0) {
            if (StringUtils.isBlank(customer.getNick())) {
                String userName = customer.getEmailAddress();
                customer.setNick(userName);
            }
            if (StringUtils.isBlank(customer.getPassword())) {
                String password = new String(com.asrevo.cvhome.store.utils.UUID.generateRandomBytes());
                String encodedPassword = /*TODO ASHRAF passwordEncoder.encode(password)*/password;
                customer.setPassword(encodedPassword);
            }
        }

   /* @TODO ASHRAF
       if (CollectionUtils.isEmpty(customer.getGroups())) {
            List<Group> groups = getListOfGroups(GroupType.CUSTOMER);
            for (Group group : groups) {
                if (group.getGroupName().equals(Constants.GROUP_CUSTOMER)) {
                    customer.getGroups().add(group);
                }
            }

        }*/

    }


    @Override
    public Address getAddress(Long userId, final MerchantStore merchantStore,
                              boolean isBillingAddress) throws Exception {
        log.info("Fetching customer for id {} ", userId);
        Address address = null;
        final Customer customerModel = customerService.getById(userId);

        if (customerModel == null) {
            log.error("Customer with ID {} does not exists..", userId);
            // throw new CustomerNotFoundException( "customer with given id does not exists" );
            throw new Exception("customer with given id does not exists");
        }

        if (isBillingAddress) {
            log.info("getting billing address..");
            CustomerBillingAddressPopulator billingAddressPopulator =
                    new CustomerBillingAddressPopulator();
            address = billingAddressPopulator.populate(customerModel, merchantStore,
                    merchantStore.getDefaultLanguage());
            address.setBillingAddress(true);
            return address;
        }

        log.info("getting Delivery address..");
        CustomerDeliveryAddressPopulator deliveryAddressPopulator =
                new CustomerDeliveryAddressPopulator();
        return deliveryAddressPopulator.populate(customerModel, merchantStore,
                merchantStore.getDefaultLanguage());

    }


    @Override
    public void updateAddress(Long userId, MerchantStore merchantStore, Address address,
                              final Language language) throws Exception {

        Customer customerModel = customerService.getById(userId);
        Map<String, Country> countriesMap = countryService.getCountriesMap(language);
        Country country = countriesMap.get(address.getCountry());

        if (customerModel == null) {
            log.error("Customer with ID {} does not exists..", userId);
            // throw new CustomerNotFoundException( "customer with given id does not exists" );
            throw new Exception("customer with given id does not exists");

        }
        if (address.isBillingAddress()) {
            log.info("updating customer billing address..");
            PersistableCustomerBillingAddressPopulator billingAddressPopulator =
                    new PersistableCustomerBillingAddressPopulator();
            customerModel = billingAddressPopulator.populate(address, customerModel, merchantStore,
                    merchantStore.getDefaultLanguage());
            customerModel.getBilling().setCountry(country);
            if (StringUtils.isNotBlank(address.getZone())) {
                Zone zone = zoneService.getByCode(address.getZone());
                if (zone == null) {
                    throw new ConversionException("Unsuported zone code " + address.getZone());
                }
                customerModel.getBilling().setZone(zone);
                customerModel.getBilling().setState(null);

            } else {
                customerModel.getBilling().setZone(null);
            }

        } else {
            log.info("updating customer shipping address..");
            PersistableCustomerShippingAddressPopulator shippingAddressPopulator =
                    new PersistableCustomerShippingAddressPopulator();
            customerModel = shippingAddressPopulator.populate(address, customerModel, merchantStore,
                    merchantStore.getDefaultLanguage());
            customerModel.getDelivery().setCountry(country);
            if (StringUtils.isNotBlank(address.getZone())) {
                Zone zone = zoneService.getByCode(address.getZone());
                if (zone == null) {
                    throw new ConversionException("Unsuported zone code " + address.getZone());
                }

                customerModel.getDelivery().setZone(zone);
                customerModel.getDelivery().setState(null);

            } else {
                customerModel.getDelivery().setZone(null);
            }

        }


        // same update address with customer model
        this.customerService.saveOrUpdate(customerModel);

    }

    @Override
    public ReadableCustomer getCustomerById(final Long id, final MerchantStore merchantStore,
                                            final Language language) {

        Customer customerModel = Optional.ofNullable(customerService.getById(id))
                .orElseThrow(() -> new ResourceNotFoundException("No Customer found for ID : " + id));

        return convertCustomerToReadableCustomer(customerModel, merchantStore, language);
    }


    @Override
    public Customer populateCustomerModel(Customer customerModel, PersistableCustomer customer,
                                          MerchantStore merchantStore, Language language) throws Exception {
        log.info("Starting to populate customer model from customer data");


        customerModel = customerPopulator.populate(customer, customerModel, merchantStore, language);

        log.info("About to persist customer to database.");
        customerService.saveOrUpdate(customerModel);
        return customerModel;
    }


    @Override
    public ReadableCustomer create(PersistableCustomer customer, MerchantStore store, Language language) {

        Assert.notNull(customer, "Customer cannot be null");
        Assert.notNull(customer.getEmailAddress(), "Customer email address is required");

        //set customer user name
        customer.setUserName(customer.getEmailAddress());
        if (userExist(customer.getUserName())) {
            throw new ServiceRuntimeException("User already exist");
        }
        //end user exists

        Customer customerToPopulate = convertPersistableCustomerToCustomer(customer, store);
        try {
            setCustomerModelDefaultProperties(customerToPopulate, store);
        } catch (Exception e) {
            throw new ServiceRuntimeException("Cannot set default customer properties", e);
        }
        saveCustomer(customerToPopulate);
        customer.setId(customerToPopulate.getId());

        notifyNewCustomer(customer, store, customerToPopulate.getDefaultLanguage());
        //convert to readable
        return convertCustomerToReadableCustomer(customerToPopulate, store, language);


    }

    private void saveCustomer(Customer customerToPopulate) {
        try {
            customerService.save(customerToPopulate);
        } catch (ServiceException exception) {
            throw new ServiceRuntimeException(exception);
        }

    }

    private boolean userExist(String userName) {
        return Optional.ofNullable(customerService.getByNick(userName))
                .isPresent();
    }

    private Customer convertPersistableCustomerToCustomer(PersistableCustomer customer, MerchantStore store) {

        Customer cust = new Customer();

        try {
            customerPopulator.populate(customer, cust, store, store.getDefaultLanguage());
        } catch (ConversionException e) {
            throw new ConversionRuntimeException(e);
        }

/*TODO ASHRAF

        List<Group> groups = getListOfGroups(GroupType.CUSTOMER);
        cust.setGroups(groups);
*/

        String password = customer.getPassword();
        if (StringUtils.isBlank(password)) {
            password = new String(com.asrevo.cvhome.store.utils.UUID.generateRandomBytes());
            customer.setPassword(password);
        }


        return cust;

    }

    @Async
    protected void notifyNewCustomer(PersistableCustomer customer, MerchantStore store, Language lang) {
        System.out.println("Customer notification");
        long startTime = System.nanoTime();
        Locale customerLocale = LocaleUtils.getLocale(lang);
        String shopSchema = coreConfiguration.getProperty("SHOP_SCHEME");
/* @TODO ASHRAF
        emailTemplatesUtils.sendRegistrationEmail(customer, store, customerLocale, shopSchema);
*/
        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000;
        System.out.println("End Notification " + duration);
    }


    private PersistableCustomer updateAuthCustomer(PersistableCustomer customer, MerchantStore store) {

        if (customer.getId() == null || customer.getId() == 0) {
            throw new ServiceRuntimeException("Can't update a customer with null id");
        }

        Customer cust = customerService.getById(customer.getId());

        try {
            customerPopulator.populate(customer, cust, store, store.getDefaultLanguage());
        } catch (ConversionException e) {
            throw new ConversionRuntimeException(e);
        }

        saveCustomer(cust);
        customer.setId(cust.getId());

        return customer;
    }


    @Override
    public PersistableCustomer update(PersistableCustomer customer, MerchantStore store) {

        if (customer.getId() == null || customer.getId() == 0) {
            throw new ServiceRuntimeException("Can't update a customer with null id");
        }

        Customer cust = customerService.getById(customer.getId());

        try {
            customerPopulator.populate(customer, cust, store, store.getDefaultLanguage());
        } catch (ConversionException e) {
            throw new ConversionRuntimeException(e);
        }

        String password = customer.getPassword();
        if (StringUtils.isBlank(password)) {
            password = new String(UUID.generateRandomBytes());
            customer.setPassword(password);
        }

        saveCustomer(cust);
        customer.setId(cust.getId());

        return customer;
    }


    @Override
    public PersistableCustomerReview saveOrUpdateCustomerReview(PersistableCustomerReview reviewTO, MerchantStore store,
                                                                Language language) {
        CustomerReview review = convertPersistableCustomerReviewToCustomerReview(reviewTO, store, language);
        createReview(review);
        reviewTO.setId(review.getId());
        return reviewTO;
    }

    private void createReview(CustomerReview review) {
        try {
            customerReviewService.create(review);
        } catch (ServiceException e) {
            throw new ServiceRuntimeException(e);
        }

    }

    private CustomerReview convertPersistableCustomerReviewToCustomerReview(
            PersistableCustomerReview review, MerchantStore store, Language language) {
        PersistableCustomerReviewPopulator populator = new PersistableCustomerReviewPopulator();
        populator.setCustomerService(customerService);
        populator.setLanguageService(languageService);
        try {
            return populator.populate(review, new CustomerReview(), store, language);
        } catch (ConversionException e) {
            throw new ConversionRuntimeException(e);
        }
    }


    @Override
    public List<ReadableCustomerReview> getAllCustomerReviewsByReviewed(Long customerId,
                                                                        MerchantStore store, Language language) {

        //customer exist
        Customer customer = getCustomerById(customerId);
        Assert.notNull(customer, "Reviewed customer cannot be null");

        return customerReviewService.getByReviewedCustomer(customer)
                .stream()
                .map(
                        customerReview ->
                                convertCustomerReviewToReadableCustomerReview(customerReview, store, language))
                .collect(Collectors.toList());
    }

    private ReadableCustomerReview convertCustomerReviewToReadableCustomerReview(
            CustomerReview customerReview, MerchantStore store, Language language) {
        try {
            ReadableCustomerReviewPopulator populator = new ReadableCustomerReviewPopulator();
            return populator.populate(customerReview, new ReadableCustomerReview(), store, language);
        } catch (ConversionException e) {
            throw new ConversionRuntimeException(e);
        }
    }

    private Customer getCustomerById(Long customerId) {
        return Optional.ofNullable(customerService.getById(customerId))
                .orElseThrow(() -> new ResourceNotFoundException("Customer id " + customerId + " does not exists"));
    }


    @Override
    public void deleteCustomerReview(Long customerId, Long reviewId, MerchantStore store, Language language) {

        CustomerReview customerReview = getCustomerReviewById(reviewId);

        if (!customerReview.getReviewedCustomer().getId().equals(customerId)) {
            throw new ResourceNotFoundException("Customer review with id " + reviewId + " does not exist for this customer");
        }
        deleteCustomerReview(customerReview);
    }

    private CustomerReview getCustomerReviewById(Long reviewId) {
        return Optional.ofNullable(customerReviewService.getById(reviewId))
                .orElseThrow(() -> new ResourceNotFoundException("Customer review with id " + reviewId + " does not exist"));
    }

    private void deleteCustomerReview(CustomerReview review) {
        try {
            customerReviewService.delete(review);
        } catch (ServiceException e) {
            throw new ServiceRuntimeException(e);
        }
    }


    @Override
    public void optinCustomer(PersistableCustomerOptin optin, MerchantStore store) {
        // check if customer optin exists
        Optin optinDef = getOptinByCode(store);

        CustomerOptin customerOptin = getCustomerOptinByEmailAddress(optin.getEmail(), store, OptinType.NEWSLETTER);

        if (customerOptin != null) {
            // exists update
            customerOptin.setEmail(optin.getEmail());
            customerOptin.setFirstName(optin.getFirstName());
            customerOptin.setLastName(optin.getLastName());
        } else {
            customerOptin = new CustomerOptin();
            customerOptin.setEmail(optin.getEmail());
            customerOptin.setFirstName(optin.getFirstName());
            customerOptin.setLastName(optin.getLastName());
            customerOptin.setOptinDate(new Date());
            customerOptin.setOptin(optinDef);
            customerOptin.setMerchantStore(store);
        }
        saveCustomerOption(customerOptin);
    }

    private void saveCustomerOption(CustomerOptin customerOptin) {
        try {
            customerOptinService.save(customerOptin);
        } catch (ServiceException e) {
            throw new ServiceRuntimeException(e);
        }
    }

    private Optin getOptinByCode(MerchantStore store) {
        try {
            return Optional.ofNullable(optinService.getOptinByCode(store, OptinType.NEWSLETTER.name()))
                    .orElseThrow(() -> new ResourceNotFoundException("Optin newsletter does not exist"));
        } catch (ServiceException e) {
            throw new ServiceRuntimeException(e);
        }
    }

    private CustomerOptin getCustomerOptinByEmailAddress(String optinEmail,
                                                         MerchantStore store, OptinType optinType) {
        try {
            return customerOptinService.findByEmailAddress(store, optinEmail, optinType.name());
        } catch (ServiceException e) {
            throw new ServiceRuntimeException(e);
        }

    }

    @Override
    public ReadableCustomer getCustomerByNick(String userName, MerchantStore merchantStore,
                                              Language language) {
        Customer customer = getByNick(userName);
        return convertCustomerToReadableCustomer(customer, merchantStore, language);
    }

    @Override
    public void deleteByNick(String userName) {
        Customer customer = getByNick(userName);
        delete(customer);
    }

    private Customer getByNick(String userName) {
        return Optional.ofNullable(customerService.getByNick(userName))
                .orElseThrow(() -> new ResourceNotFoundException("No Customer found for ID : " + userName));
    }

    @Override
    public void delete(Customer entity) {
        try {
            customerService.delete(entity);
        } catch (ServiceException e) {
            throw new ServiceRuntimeException(e);
        }
    }

    @Override
    public ReadableCustomerList getListByStore(MerchantStore store, CustomerCriteria criteria,
                                               Language language) {
        CustomerList customerList = customerService.getListByStore(store, criteria);
        return convertCustomerListToReadableCustomerList(customerList, store, language);
    }

    private ReadableCustomerList convertCustomerListToReadableCustomerList(
            CustomerList customerList, MerchantStore store, Language language) {
        List<ReadableCustomer> readableCustomers = customerList.getCustomers()
                .stream()
                .map(customer -> convertCustomerToReadableCustomer(customer, store, language))
                .collect(Collectors.toList());

        ReadableCustomerList readableCustomerList = new ReadableCustomerList();
        readableCustomerList.setCustomers(readableCustomers);
        readableCustomerList.setTotalPages(Math.toIntExact(customerList.getTotalCount()));
        return readableCustomerList;
    }

    private ReadableCustomer convertCustomerToReadableCustomer(Customer customer, MerchantStore merchantStore, Language language) {
        ReadableCustomerPopulator populator = new ReadableCustomerPopulator();
        try {
            return populator.populate(customer, new ReadableCustomer(), merchantStore, language);
        } catch (ConversionException e) {
            throw new ConversionRuntimeException(e);
        }
    }

    @Override
    public PersistableCustomerReview createCustomerReview(
            Long customerId,
            PersistableCustomerReview review,
            MerchantStore merchantStore,
            Language language) {

        // rating already exist
        Optional<CustomerReview> customerReview =
                Optional.ofNullable(
                        customerReviewService.getByReviewerAndReviewed(review.getCustomerId(), customerId));

        if (customerReview.isPresent()) {
            throw new ServiceRuntimeException("A review already exist for this customer and product");
        }

        // rating maximum 5
        if (review.getRating() > Constants.MAX_REVIEW_RATING_SCORE) {
            throw new ServiceRuntimeException("Maximum rating score is " + Constants.MAX_REVIEW_RATING_SCORE);
        }

        review.setReviewedCustomer(customerId);

        saveOrUpdateCustomerReview(review, merchantStore, language);

        return review;
    }

    @Override
    public PersistableCustomerReview updateCustomerReview(Long id, Long reviewId, PersistableCustomerReview review,
                                                          MerchantStore store, Language language) {

        CustomerReview customerReview = getCustomerReviewById(reviewId);

        if (!customerReview.getReviewedCustomer().getId().equals(id)) {
            throw new ResourceNotFoundException("Customer review with id " + reviewId + " does not exist for this customer");
        }

        //rating maximum 5
        if (review.getRating() > Constants.MAX_REVIEW_RATING_SCORE) {
            throw new ServiceRuntimeException("Maximum rating score is " + Constants.MAX_REVIEW_RATING_SCORE);
        }

        review.setReviewedCustomer(id);
        return review;
    }


    @Override
    public void deleteById(Long id) {
        Customer customer = getCustomerById(id);
        delete(customer);

    }


    @Override
    public void updateAddress(PersistableCustomer customer, MerchantStore store) {


        if (customer.getBilling() != null) {
            Assert.notNull(customer.getBilling(), "Billing address can not be null");
            Assert.notNull(customer.getBilling().getAddress(), "Billing address can not be null");
            Assert.notNull(customer.getBilling().getCity(), "Billing city can not be null");
            Assert.notNull(customer.getBilling().getPostalCode(), "Billing postal code can not be null");
            Assert.notNull(customer.getBilling().getCountry(), "Billing country can not be null");
        }
        if (customer.getDelivery() == null) {
            customer.setDelivery(customer.getBilling());
        } else {

            if (StringUtils.isBlank(customer.getDelivery().getAddress())) {
                customer.getDelivery().setAddress(customer.getBilling().getAddress());
            }
            if (StringUtils.isBlank(customer.getDelivery().getCity())) {
                customer.getDelivery().setAddress(customer.getBilling().getCity());
            }
            if (StringUtils.isBlank(customer.getDelivery().getPostalCode())) {
                customer.getDelivery().setAddress(customer.getBilling().getPostalCode());
            }
            if (StringUtils.isBlank(customer.getDelivery().getCountryCode())) {
                customer.getDelivery().setAddress(customer.getDelivery().getCountryCode());
            }
        }

        try {
            //update billing
            if (customer.getBilling() != null) {
                customer.getBilling().setBillingAddress(true);
                updateAddress(customer.getId(), store, customer.getBilling(), store.getDefaultLanguage());
            }


            //update delivery
            if (customer.getDelivery() != null) {
                customer.getDelivery().setBillingAddress(false);
                updateAddress(customer.getId(), store, customer.getDelivery(), store.getDefaultLanguage());
            }

        } catch (Exception e) {
            throw new ServiceRuntimeException("Error while updating customer address");
        }


    }


    @Override
    public void updateAddress(String userName, PersistableCustomer customer, MerchantStore store) {

        ReadableCustomer customerModel = getByUserName(userName, store, store.getDefaultLanguage());
        customer.setId(customerModel.getId());
        customer.setUserName(userName);
        updateAddress(customer, store);

    }


    @Override
    public PersistableCustomer update(String userName, PersistableCustomer customer,
                                      MerchantStore store) {
        ReadableCustomer customerModel = getByUserName(userName, store, store.getDefaultLanguage());
        customer.setId(customerModel.getId());
        customer.setUserName(userName);
        return updateAuthCustomer(customer, store);
    }


}
