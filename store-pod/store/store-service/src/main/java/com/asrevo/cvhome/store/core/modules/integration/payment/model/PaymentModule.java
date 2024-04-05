package com.asrevo.cvhome.store.core.modules.integration.payment.model;

import com.asrevo.cvhome.store.core.entity.customer.Customer;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.order.Order;
import com.asrevo.cvhome.store.core.entity.payments.Transaction;
import com.asrevo.cvhome.store.core.entity.shoppingcart.ShoppingCartItem;
import com.asrevo.cvhome.store.core.entity.system.IntegrationConfiguration;
import com.asrevo.cvhome.store.core.entity.system.IntegrationModule;
import com.asrevo.cvhome.store.core.exception.IntegrationException;
import com.asrevo.cvhome.store.core.model.payments.Payment;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentModule {

    void validateModuleConfiguration(IntegrationConfiguration integrationConfiguration, MerchantStore store) throws IntegrationException;


    /**
     * Returns token-value related to the initialization of the transaction This
     * method is invoked for paypal express checkout
     *
     * @param store         MerchantStore
     * @param customer      Customer
     * @param amount        BigDecimal
     * @param payment       Payment
     * @param configuration IntegrationConfiguration
     * @param module        IntegrationModule
     * @return Transaction a Transaction
     * @throws IntegrationException IntegrationException
     */
    Transaction initTransaction(
            MerchantStore store, Customer customer, BigDecimal amount, Payment payment, IntegrationConfiguration configuration, IntegrationModule module)
            throws IntegrationException;

    Transaction authorize(
            MerchantStore store, Customer customer, List<ShoppingCartItem> items, BigDecimal amount, Payment payment, IntegrationConfiguration configuration, IntegrationModule module)
            throws IntegrationException;


    Transaction capture(
            MerchantStore store, Customer customer, Order order, Transaction capturableTransaction, IntegrationConfiguration configuration, IntegrationModule module)
            throws IntegrationException;

    Transaction authorizeAndCapture(
            MerchantStore store, Customer customer, List<ShoppingCartItem> items, BigDecimal amount, Payment payment, IntegrationConfiguration configuration, IntegrationModule module)
            throws IntegrationException;

    Transaction refund(
            boolean partial, MerchantStore store, Transaction transaction, Order order, BigDecimal amount, IntegrationConfiguration configuration, IntegrationModule module)
            throws IntegrationException;

}
