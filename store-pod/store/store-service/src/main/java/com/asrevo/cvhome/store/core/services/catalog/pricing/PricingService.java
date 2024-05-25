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
     */
    FinalPrice calculateProductPrice(Product product) throws ServiceException;

    /**
     * Calculates variant price specific to variant inventory or parent inventory
     *
     */
    FinalPrice calculateProductPrice(ProductVariant variant) throws ServiceException;

    /**
     * Calculates the price on a specific inventory
     *
     */
    FinalPrice calculateProductPrice(ProductAvailability product) throws ServiceException;

    /**
     * Calculates the FinalPrice of a Product taking into account
     * all defined prices and possible rebates. It also applies other calculation
     * based on the customer
     *
     */
    FinalPrice calculateProductPrice(Product product, Customer customer)
            throws ServiceException;

    /**
     * Calculates the FinalPrice of a Product taking into account
     * all defined prices and possible rebates. This method should be used to calculate
     * any additional prices based on the default attributes or based on the user selected attributes.
     *
     */
    FinalPrice calculateProductPrice(Product product,
                                     List<ProductAttribute> attributes) throws ServiceException;

    /**
     * Calculates the FinalPrice of a Product taking into account
     * all defined prices and possible rebates. This method should be used to calculate
     * any additional prices based on the default attributes or based on the user selected attributes.
     * It also applies other calculation based on the customer
     *
     */
    FinalPrice calculateProductPrice(Product product,
                                     List<ProductAttribute> attributes, Customer customer)
            throws ServiceException;

    /**
     * Method to be used to print a displayable formated amount to the end user
     *
     */
    String getDisplayAmount(BigDecimal amount, MerchantStore store)
            throws ServiceException;

    /**
     * Method to be used when building an amount formatted with the appropriate currency
     *
     */
    String getDisplayAmount(BigDecimal amount, Locale locale, Currency currency, MerchantStore store)
            throws ServiceException;

    /**
     * Converts a String amount to BigDecimal
     * Takes care of String amount validation
     *
     */
    BigDecimal getAmount(String amount) throws ServiceException;

    /**
     * String format of the money amount without currency symbol
     *
     */
    String getStringAmount(BigDecimal amount, MerchantStore store)
            throws ServiceException;

    /**
     * Method for calculating sub total
     *
     */
    BigDecimal calculatePriceQuantity(BigDecimal price, int quantity);
}
