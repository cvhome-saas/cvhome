package com.asrevo.cvhome.store.service.populator.catalog;

import com.asrevo.cvhome.store.controller.exception.ConversionException;
import com.asrevo.cvhome.store.core.entity.catalog.product.price.FinalPrice;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.catalog.product.ReadableProductPrice;
import com.asrevo.cvhome.store.core.services.catalog.pricing.PricingService;
import com.asrevo.cvhome.store.utils.AbstractDataPopulator;
import org.springframework.util.Assert;

public class ReadableFinalPricePopulator extends
        AbstractDataPopulator<FinalPrice, ReadableProductPrice> {


    private PricingService pricingService;

    public PricingService getPricingService() {
        return pricingService;
    }

    public void setPricingService(PricingService pricingService) {
        this.pricingService = pricingService;
    }

    @Override
    public ReadableProductPrice populate(FinalPrice source,
                                         ReadableProductPrice target, MerchantStore store, Language language)
            throws ConversionException {
        Assert.notNull(pricingService, "pricingService must be set");

        try {

            target.setOriginalPrice(pricingService.getDisplayAmount(source.getOriginalPrice(), store));
            if (source.isDiscounted()) {
                target.setDiscounted(true);
                target.setFinalPrice(pricingService.getDisplayAmount(source.getDiscountedPrice(), store));
            } else {
                target.setFinalPrice(pricingService.getDisplayAmount(source.getFinalPrice(), store));
            }

        } catch (Exception e) {
            throw new ConversionException("Exception while converting to ReadableProductPrice", e);
        }


        return target;
    }

    @Override
    protected ReadableProductPrice createTarget() {
        // TODO Auto-generated method stub
        return null;
    }

}
