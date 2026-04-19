package com.asrevo.cvhome.catalog.services.pricing;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.entity.product.availability.ProductAvailability;
import com.asrevo.cvhome.catalog.model.product.product.price.FinalPrice;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.store.core.exception.ServiceException;
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
    public FinalPrice calculateProductPrice(Product product) throws ServiceException {
        return priceUtil.getFinalPrice(product);
    }

    @Override
    public String getDisplayAmount(BigDecimal amount, StoreMerchantId store) throws ServiceException {
        try {
            MerchantStorePricingBase merchantStore = externalMerchantStoreService.getStore(store);
            return PriceUtils.getStoreFormatedAmountWithCurrency(merchantStore, amount);
        } catch (Exception e) {
            log.error("An error occured when trying to format an amount {}", amount.toString());
            throw new ServiceException(e);
        }
    }

    @Override
    public FinalPrice calculateProductPrice(ProductAvailability availability) throws ServiceException {

        return priceUtil.getFinalPrice(availability);
    }

}
