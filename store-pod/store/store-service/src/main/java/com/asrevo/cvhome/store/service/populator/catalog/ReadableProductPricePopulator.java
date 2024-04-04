package com.asrevo.cvhome.store.service.populator.catalog;

import com.asrevo.cvhome.store.controller.exception.ConversionException;
import com.asrevo.cvhome.store.core.entity.catalog.product.price.FinalPrice;
import com.asrevo.cvhome.store.core.entity.catalog.product.price.ProductPrice;
import com.asrevo.cvhome.store.core.entity.catalog.product.price.ProductPriceDescription;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.catalog.product.ReadableProductPrice;
import com.asrevo.cvhome.store.core.model.catalog.product.ReadableProductPriceFull;
import com.asrevo.cvhome.store.core.services.catalog.pricing.PricingService;
import com.asrevo.cvhome.store.utils.AbstractDataPopulator;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@Setter
@Getter
public class ReadableProductPricePopulator extends
        AbstractDataPopulator<ProductPrice, ReadableProductPrice> {


    private PricingService pricingService;

    @Override
    public ReadableProductPrice populate(ProductPrice source,
                                         ReadableProductPrice target, MerchantStore store, Language language)
            throws ConversionException {
        Assert.notNull(pricingService, "pricingService must be set");
        Assert.notNull(source.getProductAvailability(), "productPrice.availability cannot be null");
        Assert.notNull(source.getProductAvailability().getProduct(), "productPrice.availability.product cannot be null");

        try {

            if (language == null) {
                target = new ReadableProductPriceFull();
            }

            if (source.getId() != null && source.getId() > 0) {
                target.setId(source.getId());
            }

            target.setDefaultPrice(source.isDefaultPrice());

            FinalPrice finalPrice = pricingService.calculateProductPrice(source.getProductAvailability().getProduct());

            target.setOriginalPrice(pricingService.getDisplayAmount(source.getProductPriceAmount(), store));
            if (finalPrice.isDiscounted()) {
                target.setDiscounted(true);
                target.setFinalPrice(pricingService.getDisplayAmount(source.getProductPriceSpecialAmount(), store));
            } else {
                target.setFinalPrice(pricingService.getDisplayAmount(finalPrice.getOriginalPrice(), store));
            }

            if (source.getDescriptions() != null && !source.getDescriptions().isEmpty()) {
                List<com.asrevo.cvhome.store.core.model.catalog.product.ProductPriceDescription> fulldescriptions = new ArrayList<com.asrevo.cvhome.store.core.model.catalog.product.ProductPriceDescription>();

                Set<ProductPriceDescription> descriptions = source.getDescriptions();
                ProductPriceDescription description = null;
                for (ProductPriceDescription desc : descriptions) {
                    if (language != null && desc.getLanguage().getCode().equals(language.getCode())) {
                        description = desc;
                        break;
                    } else {
                        fulldescriptions.add(populateDescription(desc));
                    }
                }


                if (description != null) {
                    com.asrevo.cvhome.store.core.model.catalog.product.ProductPriceDescription d = populateDescription(description);
                    target.setDescription(d);
                }

                if (target instanceof ReadableProductPriceFull) {
                    ((ReadableProductPriceFull) target).setDescriptions(fulldescriptions);
                }
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

    com.asrevo.cvhome.store.core.model.catalog.product.ProductPriceDescription populateDescription(
            ProductPriceDescription description) {
        if (description == null) {
            return null;
        }
        com.asrevo.cvhome.store.core.model.catalog.product.ProductPriceDescription d =
                new com.asrevo.cvhome.store.core.model.catalog.product.ProductPriceDescription();
        d.setName(description.getName());
        d.setDescription(description.getDescription());
        d.setId(description.getId());
        d.setTitle(description.getTitle());
        if (description.getLanguage() != null) {
            d.setLanguage(description.getLanguage().getCode());
        }
        return d;
    }

}
