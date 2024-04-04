package com.asrevo.cvhome.store.core.services.payments;

import com.asrevo.cvhome.store.core.entity.customer.Customer;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.order.Order;
import com.asrevo.cvhome.store.core.entity.payments.CreditCardType;
import com.asrevo.cvhome.store.core.entity.payments.Transaction;
import com.asrevo.cvhome.store.core.entity.shoppingcart.ShoppingCartItem;
import com.asrevo.cvhome.store.core.entity.system.IntegrationConfiguration;
import com.asrevo.cvhome.store.core.entity.system.IntegrationModule;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.payments.Payment;
import com.asrevo.cvhome.store.core.model.payments.PaymentMethod;
import com.asrevo.cvhome.store.core.modules.integration.payment.model.PaymentModule;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface PaymentService {

	List<IntegrationModule> getPaymentMethods(MerchantStore store)
			throws ServiceException;

	Map<String, IntegrationConfiguration> getPaymentModulesConfigured(
			MerchantStore store) throws ServiceException;
	
	Transaction processPayment(Customer customer, MerchantStore store, Payment payment, List<ShoppingCartItem> items, Order order) throws ServiceException;
	Transaction processRefund(Order order, Customer customer, MerchantStore store, BigDecimal amount) throws ServiceException;

	/**
	 * Get a specific Payment module by payment type CREDITCART, MONEYORDER ...
	 * @param store
	 * @param type (payment type)
	 * @return IntegrationModule
	 * @throws ServiceException
	 */
	IntegrationModule getPaymentMethodByType(MerchantStore store, String type)
			throws ServiceException;
	
	/**
	 * Get a specific Payment module by payment code (defined in integrationmoduel.json) paypal, authorizenet ..
	 * @param store
	 * @param name
	 * @return IntegrationModule
	 * @throws ServiceException
	 */
	IntegrationModule getPaymentMethodByCode(MerchantStore store, String name)
			throws ServiceException;

	/**
	 * Saves a payment module configuration
	 * @param configuration
	 * @param store
	 * @throws ServiceException
	 */
	void savePaymentModuleConfiguration(IntegrationConfiguration configuration,
			MerchantStore store) throws ServiceException;

	/**
	 * Validates if the credit card input information are correct
	 * @param number
	 * @param type
	 * @param month
	 * @param date
	 * @throws ServiceException
	 */
	void validateCreditCard(String number, CreditCardType creditCard, String month, String date)
			throws ServiceException;

	/**
	 * Get the integration configuration
	 * for a specific payment module
	 * @param moduleCode
	 * @param store
	 * @return IntegrationConfiguration
	 * @throws ServiceException
	 */
	IntegrationConfiguration getPaymentConfiguration(String moduleCode,
			MerchantStore store) throws ServiceException;

	void removePaymentModuleConfiguration(String moduleCode, MerchantStore store)
			throws ServiceException;

	Transaction processCapturePayment(Order order, Customer customer,
			MerchantStore store)
			throws ServiceException;
	
	/**
	 * Initializes a transaction
	 * @param order
	 * @param customer
	 * @param payment
	 * @param store
	 * @return Transaction
	 */
	Transaction initTransaction(Order order, Customer customer, Payment payment, MerchantStore store) throws ServiceException;
	
	/**
	 * Initializes a transaction without an order
	 * @param order
	 * @param customer
	 * @param payment
	 * @param store
	 * @return Transaction
	 */
	Transaction initTransaction(Customer customer, Payment payment, MerchantStore store) throws ServiceException;

	List<PaymentMethod> getAcceptedPaymentMethods(MerchantStore store)
			throws ServiceException;

	/**
	 * Returns a PaymentModule based on the payment code
	 * @param paymentModuleCode
	 * @return PaymentModule
	 * @throws ServiceException
	 */
	PaymentModule getPaymentModule(String paymentModuleCode)
			throws ServiceException;

}