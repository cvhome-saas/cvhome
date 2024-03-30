package com.asrevo.cvhome.store.service.mapper.catalog.product;

import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.entity.catalog.product.availability.ProductAvailability;
import com.asrevo.cvhome.store.core.entity.catalog.product.price.ProductPrice;
import com.asrevo.cvhome.store.core.entity.catalog.product.price.ProductPriceDescription;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.service.mapper.Mapper;
import com.asrevo.cvhome.store.core.model.catalog.product.product.PersistableProductInventory;
import com.asrevo.cvhome.store.controller.exception.ServiceRuntimeException;
import com.asrevo.cvhome.store.utils.DateUtil;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Optional;

@Component
public class PersistableProductAvailabilityMapper implements Mapper<PersistableProductInventory, ProductAvailability> {

    @Override
    public ProductAvailability convert(PersistableProductInventory source, MerchantStore store, Language language) {
        return this.merge(source, new ProductAvailability(), store, language);
    }

    @Override
    public ProductAvailability merge(PersistableProductInventory source, ProductAvailability destination,
                                     MerchantStore store, Language language) {

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
                    Date startDate;

                    startDate = DateUtil.getDate(source.getPrice().getDiscountStartDate());

                    price.setProductPriceSpecialStartDate(startDate);
                }
                if (source.getPrice().getDiscountEndDate() != null) {
                    Date endDate = DateUtil.getDate(source.getPrice().getDiscountEndDate());
                    price.setProductPriceSpecialEndDate(endDate);
                }
                destination.getPrices().add(price);
                for (Language lang : store.getLanguages()) {
                    ProductPriceDescription ppd = new ProductPriceDescription();
                    ppd.setProductPrice(price);
                    ppd.setLanguage(lang);
                    ppd.setName(ProductPriceDescription.DEFAULT_PRICE_DESCRIPTION);

                    // price appender
                    Optional<com.asrevo.cvhome.store.core.model.catalog.product.ProductPriceDescription> description = source
                            .getPrice().getDescriptions().stream()
                            .filter(d -> d.getLanguage() != null && d.getLanguage().equals(lang.getCode())).findFirst();
                    if (description.isPresent()) {
                        ppd.setPriceAppender(description.get().getPriceAppender());
                    }
                    price.getDescriptions().add(ppd);
                }

            }


        } catch (Exception e) {
            throw new ServiceRuntimeException("An error occured while mapping product availability", e);
        }

        return destination;
    }

}
