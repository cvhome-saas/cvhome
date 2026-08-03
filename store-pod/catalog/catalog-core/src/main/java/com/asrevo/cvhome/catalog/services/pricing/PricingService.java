package com.asrevo.cvhome.catalog.services.pricing;

import java.math.BigDecimal;

import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.entity.product.availability.ProductAvailability;
import com.asrevo.cvhome.catalog.errors.NoApplicableInventoryException;
import com.asrevo.cvhome.catalog.errors.ProductPriceNotConvertibleException;
import com.asrevo.cvhome.catalog.model.product.product.price.FinalPriceCalc;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

/**
 * Services for Product item price calculation.
 *
 * @author Carl Samson
 */
public interface PricingService {

    /**
     * Calculates the FinalPrice of a Product taking into account all defined prices and
     * possible rebates.
     *
     * @throws NoApplicableInventoryException the product has no inventory to price against
     */
    FinalPriceCalc calculateProductPrice(Product product) throws NoApplicableInventoryException;

    /**
     * Calculates the price on a specific inventory.
     *
     * @throws NoApplicableInventoryException the inventory carries no prices to calculate from
     */
    FinalPriceCalc calculateProductPrice(ProductAvailability product) throws NoApplicableInventoryException;

    /**
     * Method to be used to print a displayable formated amount to the end user.
     *
     * @throws ProductPriceNotConvertibleException the amount could not be rendered in the store's currency
     */
    String getDisplayAmount(BigDecimal amount, StoreMerchantId store) throws ProductPriceNotConvertibleException;

}
