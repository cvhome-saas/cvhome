package com.asrevo.cvhome.catalog.services.pricing;

import java.math.BigDecimal;

import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.entity.product.availability.ProductAvailability;
import com.asrevo.cvhome.catalog.model.product.product.price.FinalPriceCalc;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.exception.ServiceException;

/**
 * Services for Product item price calculation.
 *
 * @author Carl Samson
 */
public interface PricingService {

    /**
     * Calculates the FinalPrice of a Product taking into account all defined prices and
     * possible rebates
     */
    FinalPriceCalc calculateProductPrice(Product product) throws ServiceException;

    /**
     * Calculates the price on a specific inventory
     */
    FinalPriceCalc calculateProductPrice(ProductAvailability product) throws ServiceException;

    /**
     * Method to be used to print a displayable formated amount to the end user
     */
    String getDisplayAmount(BigDecimal amount, StoreMerchantId store) throws ServiceException;

}
