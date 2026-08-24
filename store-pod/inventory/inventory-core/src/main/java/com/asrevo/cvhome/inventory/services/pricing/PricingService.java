package com.asrevo.cvhome.inventory.services.pricing;

import java.math.BigDecimal;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.inventory.entity.ProductAvailability;
import com.asrevo.cvhome.inventory.errors.NoApplicableInventoryException;
import com.asrevo.cvhome.inventory.errors.ProductPriceNotConvertibleException;
import com.asrevo.cvhome.inventory.model.price.FinalPriceCalc;

/**
 * Services for price calculation on an availability record.
 */
public interface PricingService {

    /**
     * Calculates the price on a specific inventory.
     *
     * @throws NoApplicableInventoryException the inventory carries no prices to calculate from
     */
    FinalPriceCalc calculateProductPrice(ProductAvailability availability) throws NoApplicableInventoryException;

    /**
     * Method to be used to print a displayable formated amount to the end user.
     *
     * @throws ProductPriceNotConvertibleException the amount could not be rendered in the store's currency
     */
    String getDisplayAmount(BigDecimal amount, StoreMerchantId store) throws ProductPriceNotConvertibleException;

}
