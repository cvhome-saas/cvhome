package com.asrevo.cvhome.catalog.services.pricing;

import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.entity.product.availability.ProductAvailability;
import com.asrevo.cvhome.catalog.entity.product.price.ProductPrice;
import com.asrevo.cvhome.catalog.entity.product.variant.ProductVariant;
import com.asrevo.cvhome.catalog.model.product.product.price.FinalPrice;
import com.asrevo.cvhome.catalog.model.product.product.price.SimpleProductPrice;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.utils.PriceUtils;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * This class determines the price that is displayed in the catalogue for a
 * given item. It does not calculate the total price for a given item
 *
 * @author casams1
 */
@Slf4j
public class ProductPriceUtils {

    public FinalPrice getFinalPrice(Product product) throws ServiceException {

        FinalPrice finalPrice = calculateFinalPrice(product);

        // attributes
        /* don't understood what is the relation between att and price @TODO ashraf
                BigDecimal attributePrice = null;
                if (product.getAttributes() != null && !product.getAttributes().isEmpty()) {
                    for (ProductAttribute attribute : product.getAttributes()) {
                        if (attribute.isAttributeDefault()) {
                            if (attribute.getProductAttributePrice() != null
                                    && attribute.getProductAttributePrice().doubleValue() > 0) {
                                if (attributePrice == null) {
                                    attributePrice = new BigDecimal(0);
                                }
                                attributePrice = attributePrice.add(attribute.getProductAttributePrice());
                            }
                        }
                    }

                    if (attributePrice != null && attributePrice.doubleValue() > 0) {
                        BigDecimal fp = finalPrice.getFinalPrice();
                        fp = fp.add(attributePrice);
                        finalPrice.setFinalPrice(fp);

                        BigDecimal op = finalPrice.getOriginalPrice();
                        op = op.add(attributePrice);
                        finalPrice.setOriginalPrice(op);
                    }
                }
        */

        finalPrice.setStringPrice(PriceUtils.getStringAmount(finalPrice.getFinalPrice()));
        if (finalPrice.isDiscounted()) {
            finalPrice.setStringDiscountedPrice(
                    PriceUtils.getStringAmount(finalPrice.getDiscountedPrice()));
        }
        return finalPrice;
    }

    public FinalPrice getFinalPrice(ProductAvailability availability) throws ServiceException {

        FinalPrice finalPrice = calculateFinalPrice(availability);

        finalPrice.setStringPrice(PriceUtils.getStringAmount(finalPrice.getFinalPrice()));
        if (finalPrice.isDiscounted()) {
            finalPrice.setStringDiscountedPrice(
                    PriceUtils.getStringAmount(finalPrice.getDiscountedPrice()));
        }
        return finalPrice;
    }

    private Set<ProductAvailability> applicableAvailabilities(
            Set<ProductAvailability> availabilities) throws ServiceException {
        if (CollectionUtils.isEmpty(availabilities)) {
            throw new ServiceException(
                    ServiceException.EXCEPTION_ERROR,
                    "No applicable inventory to calculate the price.");
        }

        return availabilities.stream()
                .filter(a -> !CollectionUtils.isEmpty(a.getPrices()))
                .collect(Collectors.toSet());
    }

    private FinalPrice calculateFinalPrice(Product product) throws ServiceException {

        FinalPrice finalPrice = null;
        List<FinalPrice> otherPrices = null;

        Set<ProductAvailability> availabilities = null;
        if (!CollectionUtils.isEmpty(product.getVariants())) {
            Optional<ProductVariant> variants =
                    product.getVariants().stream()
                            .filter(ProductVariant::isDefaultSelection)
                            .findFirst();
            if (variants.isPresent()) {
                availabilities = variants.get().getAvailabilities();
                availabilities = this.applicableAvailabilities(availabilities);
            }
        }

        if (CollectionUtils.isEmpty(availabilities)) {
            availabilities = product.getAvailabilities();
            availabilities = this.applicableAvailabilities(availabilities);
        }

        for (ProductAvailability availability : availabilities) {
            if (!StringUtils.isEmpty(availability.getRegion())
                    && availability
                            .getRegion()
                            .equals(Constants.ALL_REGIONS)) { // TODO REL 2.1 accept a region
                Set<ProductPrice> prices = availability.getPrices();
                for (ProductPrice price : prices) {

                    FinalPrice p = finalPrice(price);
                    if (price.isDefaultPrice()) {
                        finalPrice = p;
                    } else {
                        if (otherPrices == null) {
                            otherPrices = new ArrayList<>();
                        }
                        otherPrices.add(p);
                    }
                }
            }
        }

        if (finalPrice != null) {
            finalPrice.setAdditionalPrices(otherPrices);
        } else {
            if (otherPrices != null) {
                finalPrice = otherPrices.getFirst();
            }
        }

        if (finalPrice == null) {
            throw new ServiceException(
                    ServiceException.EXCEPTION_ERROR,
                    "No inventory available to calculate the price. Availability should contain at"
                            + " least a region set to *");
        }

        return finalPrice;
    }

    private FinalPrice calculateFinalPrice(ProductAvailability availability)
            throws ServiceException {

        FinalPrice finalPrice = null;
        List<FinalPrice> otherPrices = null;

        Set<ProductPrice> prices = availability.getPrices();
        for (ProductPrice price : prices) {

            FinalPrice p = finalPrice(price);
            if (price.isDefaultPrice()) {
                finalPrice = p;
            } else {
                if (otherPrices == null) {
                    otherPrices = new ArrayList<>();
                }
                otherPrices.add(p);
            }
        }

        if (finalPrice != null) {
            finalPrice.setAdditionalPrices(otherPrices);
        } else {
            if (otherPrices != null) {
                finalPrice = otherPrices.getFirst();
            }
        }

        if (finalPrice == null) {
            throw new ServiceException(
                    ServiceException.EXCEPTION_ERROR,
                    "No inventory available to calculate the price. Availability should contain at"
                            + " least a region set to *");
        }

        return finalPrice;
    }

    private FinalPrice finalPrice(ProductPrice price) {

        FinalPrice finalPrice = new FinalPrice();
        BigDecimal fPrice = price.getProductPriceAmount();
        BigDecimal oPrice = price.getProductPriceAmount();

        Date today = new Date();
        // calculate discount price
        boolean hasDiscount = false;
        if (price.getProductPriceSpecialStartDate() != null
                || price.getProductPriceSpecialEndDate() != null) {

            if (price.getProductPriceSpecialStartDate() != null) {
                if (price.getProductPriceSpecialStartDate().before(today)) {
                    if (price.getProductPriceSpecialEndDate() != null) {
                        if (price.getProductPriceSpecialEndDate().after(today)) {
                            hasDiscount = true;
                            fPrice = price.getProductPriceSpecialAmount();
                            finalPrice.setDiscountEndDate(price.getProductPriceSpecialEndDate());
                        }
                    }
                }
            }

            if (!hasDiscount
                    && price.getProductPriceSpecialStartDate() == null
                    && price.getProductPriceSpecialEndDate() != null) {
                if (price.getProductPriceSpecialEndDate().after(today)) {
                    hasDiscount = true;
                    fPrice = price.getProductPriceSpecialAmount();
                    finalPrice.setDiscountEndDate(price.getProductPriceSpecialEndDate());
                }
            }
        } else {
            if (price.getProductPriceSpecialAmount() != null
                    && price.getProductPriceSpecialAmount().doubleValue() > 0) {
                hasDiscount = true;
                fPrice = price.getProductPriceSpecialAmount();
                finalPrice.setDiscountEndDate(price.getProductPriceSpecialEndDate());
            }
        }

        finalPrice.setProductPrice(toSimpleProductPrice(price));
        finalPrice.setFinalPrice(fPrice);
        finalPrice.setOriginalPrice(oPrice);

        if (price.isDefaultPrice()) {
            finalPrice.setDefaultPrice(true);
        }
        if (hasDiscount) {
            discountPrice(finalPrice);
        }

        return finalPrice;
    }

    private SimpleProductPrice toSimpleProductPrice(ProductPrice price) {
        SimpleProductPrice simpleProductPrice = new SimpleProductPrice();
        simpleProductPrice.setCode(price.getCode());
        simpleProductPrice.setDefaultPrice(price.isDefaultPrice());
        simpleProductPrice.setProductPriceSpecialAmount(price.getProductPriceSpecialAmount());
        simpleProductPrice.setProductPriceSpecialStartDate(price.getProductPriceSpecialStartDate());
        simpleProductPrice.setProductPriceSpecialEndDate(price.getProductPriceSpecialEndDate());
        simpleProductPrice.setProductPriceAmount(price.getProductPriceAmount());
        simpleProductPrice.setProductPriceType(price.getProductPriceType());
        return simpleProductPrice;
    }

    private void discountPrice(FinalPrice finalPrice) {

        finalPrice.setDiscounted(true);

        double arith =
                finalPrice.getProductPrice().getProductPriceSpecialAmount().doubleValue()
                        / finalPrice.getProductPrice().getProductPriceAmount().doubleValue();
        double fsdiscount = 100 - (arith * 100);
        float percentagediscount = Double.valueOf(fsdiscount).floatValue();
        int percent = (int) percentagediscount;
        finalPrice.setDiscountPercent(percent);

        // calculate percent
        finalPrice.setDiscountedPrice(finalPrice.getProductPrice().getProductPriceSpecialAmount());
    }
}
