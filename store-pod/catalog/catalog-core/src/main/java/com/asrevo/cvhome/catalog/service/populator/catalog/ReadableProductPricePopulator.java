package com.asrevo.cvhome.catalog.service.populator.catalog;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.asrevo.cvhome.catalog.entity.product.price.ProductPrice;
import com.asrevo.cvhome.catalog.entity.product.price.ProductPriceDescription;
import com.asrevo.cvhome.catalog.errors.NoApplicableInventoryException;
import com.asrevo.cvhome.catalog.errors.ProductPriceNotConvertibleException;
import com.asrevo.cvhome.catalog.model.product.ReadableProductPrice;
import com.asrevo.cvhome.catalog.model.product.ReadableProductPriceFull;
import com.asrevo.cvhome.catalog.model.product.product.price.FinalPriceCalc;
import com.asrevo.cvhome.catalog.services.pricing.PricingService;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.populator.AbstractDataPopulator;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReadableProductPricePopulator
        extends AbstractDataPopulator<ProductPrice, StoreMerchantId, ReadableProductPrice> {

    private PricingService pricingService;

    /**
     * Narrows the inherited two-argument form back to this populator's own failure type. {@code AbstractDataPopulator}
     * declares the shared category base so that a migrated populator can name its own conditions; without this
     * override, callers of the short form would have to handle a base none of them can act on.
     */
    @Override
    public ReadableProductPrice populate(ProductPrice source, StoreMerchantId store, LanguageCode language)
            throws ProductPriceNotConvertibleException {
        // Not super.populate(...): that is the frame declaring the wide base. Re-dispatching to the four-argument
        // override here is what keeps the narrow type on this signature.
        return populate(source, createTarget(), store, language);
    }

    @Override
    public ReadableProductPrice populate(ProductPrice source, ReadableProductPrice target, StoreMerchantId store,
                                         LanguageCode language) throws ProductPriceNotConvertibleException {
        try {

            if (language == null) {
                target = new ReadableProductPriceFull();
            }

            if (source.getId() != null && source.getId() > 0) {
                target.setId(source.getId());
            }

            target.setDefaultPrice(source.isDefaultPrice());

            applyFinalPrice(source, target, store);
            applyDescriptions(source, target, language);

        } catch (Exception e) {
            throw ProductPriceNotConvertibleException.of(e);
        }

        return target;
    }

    private void applyFinalPrice(ProductPrice source, ReadableProductPrice target, StoreMerchantId store)
            throws NoApplicableInventoryException, ProductPriceNotConvertibleException {
        FinalPriceCalc finalPrice = pricingService.calculateProductPrice(source.getProductAvailability().getProduct());

        target.setOriginalPrice(pricingService.getDisplayAmount(source.getProductPriceAmount(), store));
        if (finalPrice.isDiscounted()) {
            target.setDiscounted(true);
            target.setFinalPrice(pricingService.getDisplayAmount(source.getProductPriceSpecialAmount(), store));
        } else {
            target.setFinalPrice(pricingService.getDisplayAmount(finalPrice.getOriginalPrice(), store));
        }
    }

    private void applyDescriptions(ProductPrice source, ReadableProductPrice target, LanguageCode language) {
        if (source.getDescriptions() == null || source.getDescriptions().isEmpty()) {
            return;
        }
        List<com.asrevo.cvhome.catalog.model.product.ProductPriceDescription> fulldescriptions = new ArrayList<>();

        Set<ProductPriceDescription> descriptions = source.getDescriptions();
        ProductPriceDescription description = null;
        for (ProductPriceDescription desc : descriptions) {
            if (desc.getLanguageCode().equals(language)) {
                description = desc;
                break;
            } else {
                fulldescriptions.add(populateDescription(desc));
            }
        }

        if (description != null) {
            com.asrevo.cvhome.catalog.model.product.ProductPriceDescription d = populateDescription(description);
            target.setDescription(d);
        }

        if (target instanceof ReadableProductPriceFull it) {
            it.setDescriptions(fulldescriptions);
        }
    }

    @Override
    protected ReadableProductPrice createTarget() {

        return null;
    }

    com.asrevo.cvhome.catalog.model.product.ProductPriceDescription populateDescription(
            ProductPriceDescription description) {
        if (description == null) {
            return null;
        }
        com.asrevo.cvhome.catalog.model.product.ProductPriceDescription d =
                new com.asrevo.cvhome.catalog.model.product.ProductPriceDescription();
        d.setName(description.getName());
        d.setDescription(description.getDescription());
        d.setId(description.getId());
        d.setTitle(description.getTitle());
        d.setLanguage(description.getLanguageCode());
        return d;
    }

}
