package com.asrevo.cvhome.catalog.service.mapper.catalog.product;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.entity.product.availability.ProductAvailability;
import com.asrevo.cvhome.catalog.model.product.ReadableProduct;
import com.asrevo.cvhome.catalog.model.product.product.price.FinalPriceCalc;
import com.asrevo.cvhome.catalog.services.pricing.PricingService;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.controller.exception.ConversionRuntimeException;
import com.asrevo.cvhome.store.core.mapper.Mapper;

/**
 * Works for product v2 model
 *
 * @author carlsamson
 */
@Component
public class ReadableBaseProductMapper implements Mapper<Product, ReadableProduct> {

    private final PricingService pricingService;

    public ReadableBaseProductMapper(PricingService pricingService) {
        this.pricingService = pricingService;
    }

    @Override
    public ReadableProduct convert(Product source, StoreMerchantId store, LanguageCode language) {
        ReadableProduct product = new ReadableProduct();
        return this.merge(source, product, store, language);
    }

    @Override
    public ReadableProduct merge(Product source, ReadableProduct destination, StoreMerchantId store,
                                 LanguageCode language) {

        destination.setSku(source.getSku());
        destination.setRefSku(source.getRefSku());
        destination.setId(source.getId());
        destination.setDateAvailable(source.getDateAvailable());

        destination.setId(source.getId());
        destination.setAvailable(source.isAvailable());
        destination.setProductShipeable(source.isProductShipeable());

        destination.setPreOrder(source.isPreOrder());
        destination.setRefSku(source.getRefSku());
        destination.setSortOrder(source.getSortOrder());

        if (source.getDateAvailable() != null) {
            destination.setDateAvailable(source.getDateAvailable());
        }

        if (source.getAuditSection() != null) {
            destination.setCreationDate(source.getAuditSection().getDateCreated());
        }

        destination.setProductVirtual(source.isProductVirtual());

        if (source.getProductReviewCount() != null) {
            destination.setRatingCount(source.getProductReviewCount());
        }

        applyAvailability(source, destination);

        // if default instance

        destination.setSku(source.getSku());

        applyPrice(source, destination, store);

        destination.setSortOrder(source.getSortOrder());

        return destination;
    }

    private void applyAvailability(Product source, ReadableProduct destination) {
        for (ProductAvailability a : source.getAvailabilities()) {

            destination.setQuantity(Optional.ofNullable(a.getProductQuantity()).orElse(1));
            destination.setQuantityOrderMaximum(Optional.ofNullable(a.getProductQuantityOrderMax()).orElse(1));
            destination.setQuantityOrderMinimum(Optional.ofNullable(a.getProductQuantityOrderMin()).orElse(1));
            if (a.getProductQuantity() > 0 && destination.isAvailable()) {
                destination.setCanBePurchased(true);
            }

            if (a.getProductVariant() == null && StringUtils.isEmpty(a.getRegionVariant())) {
                break;
            }
        }
    }

    private void applyPrice(Product source, ReadableProduct destination, StoreMerchantId store) {
        try {
            FinalPriceCalc price = pricingService.calculateProductPrice(source);
            if (price != null) {

                destination.setFinalPrice(pricingService.getDisplayAmount(price.getFinalPrice(), store));
                destination.setPrice(price.getFinalPrice());
                destination.setOriginalPrice(pricingService.getDisplayAmount(price.getOriginalPrice(), store));

                if (price.isDiscounted()) {
                    destination.setDiscounted(true);
                }
            }

        } catch (Exception e) {
            throw new ConversionRuntimeException("An error while converting product price", e);
        }
    }

}
