package com.asrevo.cvhome.store.core.services.catalog.pricing;

import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import com.asrevo.cvhome.store.core.entity.catalog.product.attribute.ProductAttribute;
import com.asrevo.cvhome.store.core.entity.catalog.product.availability.ProductAvailability;
import com.asrevo.cvhome.store.core.entity.catalog.product.price.FinalPrice;
import com.asrevo.cvhome.store.core.entity.catalog.product.variant.ProductVariant;
import com.asrevo.cvhome.store.core.entity.customer.Customer;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.currency.Currency;
import com.asrevo.cvhome.store.core.exception.ServiceException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;


/**
 * Services for Product item price calculation.
 *
 * @author Carl Samson
 */
public interface PricingService {

    /**
     * Calculates the FinalPrice of a Product taking into account
     * all defined prices and possible rebates
     *
     * @param product
     * @return
     * @throws ServiceException
     */
    FinalPrice calculateProductPrice(Product product) throws ServiceException;

    /**
     * Calculates variant price specific to variant inventory or parent inventory
     *
     * @param variant
     * @return
     * @throws ServiceException
     */
    FinalPrice calculateProductPrice(ProductVariant variant) throws ServiceException;

    /**
     * Calculates the price on a specific inventory
     *
     * @param product
     * @return
     * @throws ServiceException
     */
    FinalPrice calculateProductPrice(ProductAvailability product) throws ServiceException;

    /**
     * Calculates the FinalPrice of a Product taking into account
     * all defined prices and possible rebates. It also applies other calculation
     * based on the customer
     *
     * @param product
     * @param customer
     * @return
     * @throws ServiceException
     */
    FinalPrice calculateProductPrice(Product product, Customer customer)
            throws ServiceException;

    /**
     * Calculates the FinalPrice of a Product taking into account
     * all defined prices and possible rebates. This method should be used to calculate
     * any additional prices based on the default attributes or based on the user selected attributes.
     *
     * @param product
     * @param attributes
     * @return
     * @throws ServiceException
     */
    FinalPrice calculateProductPrice(Product product,
                                     List<ProductAttribute> attributes) throws ServiceException;

    /**
     * Calculates the FinalPrice of a Product taking into account
     * all defined prices and possible rebates. This method should be used to calculate
     * any additional prices based on the default attributes or based on the user selected attributes.
     * It also applies other calculation based on the customer
     *
     * @param product
     * @param attributes
     * @param customer
     * @return
     * @throws ServiceException
     */
    FinalPrice calculateProductPrice(Product product,
                                     List<ProductAttribute> attributes, Customer customer)
            throws ServiceException;

    /**
     * Method to be used to print a displayable formated amount to the end user
     *
     * @param amount
     * @param store
     * @return
     * @throws ServiceException
     */
    String getDisplayAmount(BigDecimal amount, MerchantStore store)
            throws ServiceException;

    /**
     * Method to be used when building an amount formatted with the appropriate currency
     *
     * @param amount
     * @param locale
     * @param currency
     * @param store
     * @return
     * @throws ServiceException
     */
    String getDisplayAmount(BigDecimal amount, Locale locale, Currency currency, MerchantStore store)
            throws ServiceException;

    /**
     * Converts a String amount to BigDecimal
     * Takes care of String amount validation
     *
     * @param amount
     * @return
     * @throws ServiceException
     */
    BigDecimal getAmount(String amount) throws ServiceException;

    /**
     * String format of the money amount without currency symbol
     *
     * @param amount
     * @param store
     * @return
     * @throws ServiceException
     */
    String getStringAmount(BigDecimal amount, MerchantStore store)
            throws ServiceException;

    /**
     * Method for calculating sub total
     *
     * @param price
     * @param quantity
     * @return
     */
    BigDecimal calculatePriceQuantity(BigDecimal price, int quantity);
}
