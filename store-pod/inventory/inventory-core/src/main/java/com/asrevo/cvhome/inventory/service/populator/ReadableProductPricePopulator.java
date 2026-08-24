package com.asrevo.cvhome.inventory.service.populator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.inventory.entity.ProductPrice;
import com.asrevo.cvhome.inventory.entity.ProductPriceDescription;
import com.asrevo.cvhome.inventory.errors.NoApplicableInventoryException;
import com.asrevo.cvhome.inventory.errors.ProductPriceNotConvertibleException;
import com.asrevo.cvhome.inventory.model.price.FinalPriceCalc;
import com.asrevo.cvhome.inventory.model.price.ReadableProductPrice;
import com.asrevo.cvhome.inventory.model.price.ReadableProductPriceFull;
import com.asrevo.cvhome.inventory.services.pricing.PricingService;
import com.asrevo.cvhome.store.core.populator.AbstractDataPopulator;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReadableProductPricePopulator
        extends AbstractDataPopulator<ProductPrice, StoreMerchantId, ReadableProductPrice> {

    private PricingService pricingService;

    /**
     * Narrows the inherited two-argument form back to this populator's own failure type.
     */
    @Override
    public ReadableProductPrice populate(ProductPrice source, StoreMerchantId store, LanguageCode language)
            throws ProductPriceNotConvertibleException {
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
        // Priced against the availability the price hangs off — the product entity is out of reach since the split.
        FinalPriceCalc finalPrice = pricingService.calculateProductPrice(source.getProductAvailability());

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
        List<com.asrevo.cvhome.inventory.model.price.ProductPriceDescription> fulldescriptions = new ArrayList<>();

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
            com.asrevo.cvhome.inventory.model.price.ProductPriceDescription d = populateDescription(description);
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

    com.asrevo.cvhome.inventory.model.price.ProductPriceDescription populateDescription(
            ProductPriceDescription description) {
        if (description == null) {
            return null;
        }
        com.asrevo.cvhome.inventory.model.price.ProductPriceDescription d =
                new com.asrevo.cvhome.inventory.model.price.ProductPriceDescription();
        d.setName(description.getName());
        d.setDescription(description.getDescription());
        d.setId(description.getId());
        d.setTitle(description.getTitle());
        d.setLanguage(description.getLanguageCode());
        return d;
    }

}
