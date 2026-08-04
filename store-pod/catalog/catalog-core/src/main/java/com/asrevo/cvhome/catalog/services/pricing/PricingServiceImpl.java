package com.asrevo.cvhome.catalog.services.pricing;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.entity.product.availability.ProductAvailability;
import com.asrevo.cvhome.catalog.errors.NoApplicableInventoryException;
import com.asrevo.cvhome.catalog.errors.ProductPriceNotConvertibleException;
import com.asrevo.cvhome.catalog.model.product.product.price.FinalPriceCalc;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.store.core.model.MerchantStorePricingBase;
import com.asrevo.cvhome.store.utils.PriceUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Contains all the logic required to calculate product price
 *
 * @author Carl Samson
 */
@Service("pricingService")
@Slf4j
public class PricingServiceImpl implements PricingService {

    private final ProductPriceUtils priceUtil;

    private final ExternalMerchantStoreService externalMerchantStoreService;

    public PricingServiceImpl(ExternalMerchantStoreService externalMerchantStoreService) {
        this.externalMerchantStoreService = externalMerchantStoreService;
        this.priceUtil = new ProductPriceUtils();
    }

    @Override
    public FinalPriceCalc calculateProductPrice(Product product) throws NoApplicableInventoryException {
        return priceUtil.getFinalPrice(product);
    }

    @Override
    public String getDisplayAmount(BigDecimal amount, StoreMerchantId store)
            throws ProductPriceNotConvertibleException {
        try {
            MerchantStorePricingBase merchantStore = externalMerchantStoreService.getStore(store);
            return PriceUtils.getStoreFormatedAmountWithCurrency(merchantStore, amount);
        } catch (Exception e) {
            log.error("An error occured when trying to format an amount {}", amount.toString());
            throw ProductPriceNotConvertibleException.of(amount, e);
        }
    }

    @Override
    public FinalPriceCalc calculateProductPrice(ProductAvailability availability) throws NoApplicableInventoryException {

        return priceUtil.getFinalPrice(availability);
    }

}
