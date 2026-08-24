package com.asrevo.cvhome.inventory.services.pricing;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.asrevo.cvhome.inventory.entity.ProductAvailability;
import com.asrevo.cvhome.inventory.entity.ProductPrice;
import com.asrevo.cvhome.inventory.errors.NoApplicableInventoryException;
import com.asrevo.cvhome.inventory.model.price.FinalPriceCalc;
import com.asrevo.cvhome.inventory.model.price.ProductPriceType;
import com.asrevo.cvhome.inventory.model.price.SimpleProductPrice;
import com.asrevo.cvhome.store.utils.PriceUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Determines the price that is displayed in the catalogue for a given availability. It does not calculate the total
 * price for a given item.
 *
 * <p>
 * The former product-level overloads (default variant resolution, region filtering across a product's availabilities)
 * left with the catalog split — an availability row is now the unit prices hang off.
 * </p>
 */
@Slf4j
public class ProductPriceUtils {

    public FinalPriceCalc getFinalPrice(ProductAvailability availability) throws NoApplicableInventoryException {

        FinalPriceCalc finalPrice = calculateFinalPrice(availability);
        finalPrice.setSku(availability.getSku());

        finalPrice.setStringPrice(PriceUtils.getStringAmount(finalPrice.getFinalPrice()));
        if (finalPrice.isDiscounted()) {
            finalPrice.setStringDiscountedPrice(PriceUtils.getStringAmount(finalPrice.getDiscountedPrice()));
        }
        return finalPrice;
    }

    private FinalPriceCalc calculateFinalPrice(ProductAvailability availability) throws NoApplicableInventoryException {
        return buildFinalPriceFromPrices(availability.getSku(), availability.getPrices());
    }

    private FinalPriceCalc buildFinalPriceFromPrices(String sku, Collection<ProductPrice> prices)
            throws NoApplicableInventoryException {

        FinalPriceCalc finalPrice = null;
        List<FinalPriceCalc> otherPrices = null;

        for (ProductPrice price : prices) {

            FinalPriceCalc p = finalPrice(price);
            if (price.isDefaultPrice()) {
                finalPrice = p;
                continue;
            }
            if (otherPrices == null) {
                otherPrices = new ArrayList<>();
            }
            otherPrices.add(p);
        }

        if (finalPrice != null) {
            finalPrice.setAdditionalPrices(otherPrices);
        } else if (otherPrices != null) {
            finalPrice = otherPrices.getFirst();
        }

        if (finalPrice == null) {
            throw NoApplicableInventoryException.of(sku);
        }

        return finalPrice;
    }

    private FinalPriceCalc finalPrice(ProductPrice price) {

        FinalPriceCalc finalPrice = new FinalPriceCalc();
        BigDecimal oPrice = price.getProductPriceAmount();
        LocalDate today = LocalDate.now();

        // calculate discount price
        Optional<BigDecimal> discountedAmount = resolveDiscountedAmount(price, today, finalPrice);
        BigDecimal fPrice = discountedAmount.orElse(price.getProductPriceAmount());

        finalPrice.setProductPrice(toSimpleProductPrice(price));
        finalPrice.setFinalPrice(fPrice);
        finalPrice.setOriginalPrice(oPrice);

        if (price.isDefaultPrice()) {
            finalPrice.setDefaultPrice(true);
        }
        if (discountedAmount.isPresent()) {
            discountPrice(finalPrice);
        }

        return finalPrice;
    }

    private Optional<BigDecimal> resolveDiscountedAmount(ProductPrice price, LocalDate today,
                                                         FinalPriceCalc finalPrice) {
        LocalDate specialStart = price.getProductPriceSpecialStartDate();
        LocalDate specialEnd = price.getProductPriceSpecialEndDate();

        if (specialStart == null && specialEnd == null) {
            return resolveUnscheduledDiscount(price, finalPrice, specialEnd);
        }

        if (isDiscountActive(specialStart, specialEnd, today)) {
            finalPrice.setDiscountEndDate(specialEnd);
            return Optional.of(price.getProductPriceSpecialAmount());
        }

        return Optional.empty();
    }

    private Optional<BigDecimal> resolveUnscheduledDiscount(ProductPrice price, FinalPriceCalc finalPrice,
                                                            LocalDate specialEnd) {
        if (price.getProductPriceSpecialAmount() != null && price.getProductPriceSpecialAmount().doubleValue() > 0) {
            finalPrice.setDiscountEndDate(specialEnd);
            return Optional.of(price.getProductPriceSpecialAmount());
        }
        return Optional.empty();
    }

    private boolean isDiscountActive(LocalDate specialStart, LocalDate specialEnd, LocalDate today) {
        return isDiscountWindowActive(specialStart, specialEnd, today)
                || isOpenEndedDiscountActive(specialStart, specialEnd, today);
    }

    private boolean isDiscountWindowActive(LocalDate specialStart, LocalDate specialEnd, LocalDate today) {
        return specialStart != null && specialEnd != null
                && specialStart.isBefore(today) && specialEnd.isAfter(today);
    }

    private boolean isOpenEndedDiscountActive(LocalDate specialStart, LocalDate specialEnd, LocalDate today) {
        return specialStart == null && specialEnd != null && specialEnd.isAfter(today);
    }

    private SimpleProductPrice toSimpleProductPrice(ProductPrice price) {
        SimpleProductPrice simpleProductPrice = new SimpleProductPrice();
        simpleProductPrice.setCode(price.getCode());
        simpleProductPrice.setDefaultPrice(price.isDefaultPrice());
        simpleProductPrice.setProductPriceSpecialAmount(price.getProductPriceSpecialAmount());
        simpleProductPrice.setProductPriceSpecialStartDate(price.getProductPriceSpecialStartDate());
        simpleProductPrice.setProductPriceSpecialEndDate(price.getProductPriceSpecialEndDate());
        simpleProductPrice.setProductPriceAmount(price.getProductPriceAmount());
        ProductPriceType type = price.getProductPriceType();
        simpleProductPrice.setProductPriceType(type);
        simpleProductPrice.setDefaultPrice(price.isDefaultPrice());
        return simpleProductPrice;
    }

    private void discountPrice(FinalPriceCalc finalPrice) {

        finalPrice.setDiscounted(true);

        double arith = finalPrice.getProductPrice().getProductPriceSpecialAmount().doubleValue()
                / finalPrice.getProductPrice().getProductPriceAmount().doubleValue();
        double fsdiscount = 100 - (arith * 100);
        float percentagediscount = Double.valueOf(fsdiscount).floatValue();
        int percent = (int) percentagediscount;
        finalPrice.setDiscountPercent(percent);

        // calculate percent
        finalPrice.setDiscountedPrice(finalPrice.getProductPrice().getProductPriceSpecialAmount());
    }

}
