package com.asrevo.cvhome.catalog.service.mapper.catalog.product;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.asrevo.cvhome.catalog.entity.product.availability.ProductAvailability;
import com.asrevo.cvhome.catalog.entity.product.price.ProductPrice;
import com.asrevo.cvhome.catalog.entity.product.price.ProductPriceDescription;
import com.asrevo.cvhome.catalog.model.product.product.PersistableProductInventory;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.merchant.model.merchant.ReadableMerchantStore;
import com.asrevo.cvhome.store.controller.exception.ServiceRuntimeException;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.mapper.Mapper;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class PersistableProductAvailabilityMapper implements Mapper<PersistableProductInventory, ProductAvailability> {

    private final ExternalMerchantStoreService externalMerchantStoreService;

    @Override
    public ProductAvailability convert(PersistableProductInventory source, StoreMerchantId store,
                                       LanguageCode language) {
        return this.merge(source, new ProductAvailability(), store, language);
    }

    @Override
    public ProductAvailability merge(PersistableProductInventory source, ProductAvailability destination,
                                     StoreMerchantId store, LanguageCode language) {

        try {

            destination.setRegion(Constants.ALL_REGIONS);

            destination.setProductQuantity(source.getQuantity());
            destination.setProductQuantityOrderMin(1);
            destination.setProductQuantityOrderMax(1);

            if (source.getPrice() != null) {

                ProductPrice price = new ProductPrice();
                price.setProductAvailability(destination);
                price.setDefaultPrice(source.getPrice().isDefaultPrice());
                price.setProductPriceAmount(source.getPrice().getPrice());
                price.setCode(source.getPrice().getCode());
                price.setProductPriceSpecialAmount(source.getPrice().getDiscountedPrice());
                if (source.getPrice().getDiscountStartDate() != null) {
                    LocalDate startDate;

                    startDate = source.getPrice().getDiscountStartDate();

                    price.setProductPriceSpecialStartDate(startDate);
                }
                if (source.getPrice().getDiscountEndDate() != null) {
                    LocalDate endDate = source.getPrice().getDiscountEndDate();
                    price.setProductPriceSpecialEndDate(endDate);
                }

                destination.getPrices().add(price);
                ReadableMerchantStore baseStore = externalMerchantStoreService.getStore(store);

                List<LanguageCode> supportedLanguages = baseStore.getSupportedLanguages()
                        .stream()
                        .map(LanguageCode::new)
                        .toList();

                for (LanguageCode lang : supportedLanguages) {
                    ProductPriceDescription ppd = new ProductPriceDescription();
                    ppd.setProductPrice(price);
                    ppd.setLanguageCode(lang);
                    ppd.setName(Constants.DEFAULT_PRICE_DESCRIPTION);

                    // price appender
                    Optional<com.asrevo.cvhome.catalog.model.product.ProductPriceDescription> description = source
                            .getPrice()
                            .getDescriptions()
                            .stream()
                            .filter(d -> d.getLanguage() != null && d.getLanguage().equals(lang))
                            .findFirst();
                    description.ifPresent(productPriceDescription -> ppd
                            .setPriceAppender(productPriceDescription.getPriceAppender()));
                    price.getDescriptions().add(ppd);
                }
            }

        } catch (Exception e) {
            throw new ServiceRuntimeException("An error occured while mapping product availability", e);
        }

        return destination;
    }

}
