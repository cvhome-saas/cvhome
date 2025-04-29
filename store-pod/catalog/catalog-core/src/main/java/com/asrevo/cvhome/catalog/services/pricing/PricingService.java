package com.asrevo.cvhome.catalog.services.pricing;

import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.entity.product.availability.ProductAvailability;
import com.asrevo.cvhome.catalog.model.product.product.price.FinalPrice;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import java.math.BigDecimal;

/**
 * Services for Product item price calculation.
 *
 * @author Carl Samson
 */
public interface PricingService {

    /**
     * Calculates the FinalPrice of a Product taking into account
     * all defined prices and possible rebates
     */
    FinalPrice calculateProductPrice(Product product) throws ServiceException;

    /**
     * Calculates the price on a specific inventory
     */
    FinalPrice calculateProductPrice(ProductAvailability product) throws ServiceException;

    /**
     * Method to be used to print a displayable formated amount to the end user
     */
    String getDisplayAmount(BigDecimal amount, StoreMerchantId store) throws ServiceException;
}
