package com.asrevo.cvhome.catalog.service.populator.catalog;


import com.asrevo.cvhome.catalog.model.product.ReadableProductPrice;
import com.asrevo.cvhome.catalog.model.product.product.price.FinalPriceCalc;
import com.asrevo.cvhome.catalog.services.pricing.PricingService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.exception.ConversionException;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.store.core.populator.AbstractDataPopulator;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReadableFinalPricePopulator
        extends AbstractDataPopulator<FinalPriceCalc, StoreMerchantId, ReadableProductPrice> {

    private PricingService pricingService;

    @Override
    public ReadableProductPrice populate(FinalPriceCalc source, ReadableProductPrice target, StoreMerchantId store,
                                         LanguageCode language) throws ConversionException {
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

        return null;
    }

}
