package com.asrevo.cvhome.catalog.services.pricing;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.entity.product.availability.ProductAvailability;
import com.asrevo.cvhome.catalog.entity.product.price.ProductPrice;
import com.asrevo.cvhome.catalog.entity.product.variant.ProductVariant;
import com.asrevo.cvhome.catalog.model.product.product.price.FinalPriceCalc;
import com.asrevo.cvhome.catalog.model.product.product.price.SimpleProductPrice;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.utils.PriceUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * This class determines the price that is displayed in the catalogue for a given item. It
 * does not calculate the total price for a given item
 *
 * @author casams1
 */
@Slf4j
public class ProductPriceUtils {

    private static final String NO_INVENTORY_MESSAGE =
            "No inventory available to calculate the price. Availability should contain at least a region set to *";

    public FinalPriceCalc getFinalPrice(Product product) throws ServiceException {

        FinalPriceCalc finalPrice = calculateFinalPrice(product);

        finalPrice.setStringPrice(PriceUtils.getStringAmount(finalPrice.getFinalPrice()));
        if (finalPrice.isDiscounted()) {
            finalPrice.setStringDiscountedPrice(PriceUtils.getStringAmount(finalPrice.getDiscountedPrice()));
        }
        return finalPrice;
    }

    public FinalPriceCalc getFinalPrice(ProductAvailability availability) throws ServiceException {

        FinalPriceCalc finalPrice = calculateFinalPrice(availability);

        finalPrice.setStringPrice(PriceUtils.getStringAmount(finalPrice.getFinalPrice()));
        if (finalPrice.isDiscounted()) {
            finalPrice.setStringDiscountedPrice(PriceUtils.getStringAmount(finalPrice.getDiscountedPrice()));
        }
        return finalPrice;
    }

    private Set<ProductAvailability> applicableAvailabilities(Set<ProductAvailability> availabilities)
            throws ServiceException {
        if (CollectionUtils.isEmpty(availabilities)) {
            throw new ServiceException(ServiceException.EXCEPTION_ERROR,
                    "No applicable inventory to calculate the price.");
        }

        return availabilities.stream().filter(a -> !CollectionUtils.isEmpty(a.getPrices())).collect(Collectors.toSet());
    }

    private FinalPriceCalc calculateFinalPrice(Product product) throws ServiceException {

        Set<ProductAvailability> availabilities = resolveApplicableAvailabilities(product);

        List<ProductPrice> applicablePrices = new ArrayList<>();
        for (ProductAvailability availability : availabilities) {
            if (!isAllRegionsAvailability(availability)) {
                continue;
            }
            applicablePrices.addAll(availability.getPrices());
        }

        FinalPriceCalc finalPrice = buildFinalPriceFromPrices(applicablePrices);
        finalPrice.setSku(product.getSku());
        return finalPrice;
    }

    private Set<ProductAvailability> resolveApplicableAvailabilities(Product product) throws ServiceException {
        Set<ProductAvailability> availabilities = null;
        if (!CollectionUtils.isEmpty(product.getVariants())) {
            Optional<ProductVariant> variants = product.getVariants()
                    .stream()
                    .filter(ProductVariant::isDefaultSelection)
                    .findFirst();
            if (variants.isPresent()) {
                availabilities = this.applicableAvailabilities(variants.get().getAvailabilities());
            }
        }

        if (CollectionUtils.isEmpty(availabilities)) {
            availabilities = this.applicableAvailabilities(product.getAvailabilities());
        }
        return availabilities;
    }

    private boolean isAllRegionsAvailability(ProductAvailability availability) {
        return !StringUtils.isEmpty(availability.getRegion()) // TODO
                && availability.getRegion().equals(Constants.ALL_REGIONS);
    }

    private FinalPriceCalc calculateFinalPrice(ProductAvailability availability) throws ServiceException {
        return buildFinalPriceFromPrices(availability.getPrices());
    }

    private FinalPriceCalc buildFinalPriceFromPrices(Collection<ProductPrice> prices) throws ServiceException {

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
            throw new ServiceException(ServiceException.EXCEPTION_ERROR, NO_INVENTORY_MESSAGE);
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

    private Optional<BigDecimal> resolveDiscountedAmount(ProductPrice price, LocalDate today, FinalPriceCalc finalPrice) {
        LocalDate specialStart = price.getProductPriceSpecialStartDate();
        LocalDate specialEnd = price.getProductPriceSpecialEndDate();

        if (specialStart == null && specialEnd == null) {
            if (price.getProductPriceSpecialAmount() != null && price.getProductPriceSpecialAmount().doubleValue() > 0) {
                finalPrice.setDiscountEndDate(specialEnd);
                return Optional.of(price.getProductPriceSpecialAmount());
            }
            return Optional.empty();
        }

        boolean discountWindowActive = specialStart != null && specialEnd != null
                && specialStart.isBefore(today) && specialEnd.isAfter(today);
        boolean openEndedDiscountActive = specialStart == null && specialEnd != null && specialEnd.isAfter(today);

        if (discountWindowActive || openEndedDiscountActive) {
            finalPrice.setDiscountEndDate(specialEnd);
            return Optional.of(price.getProductPriceSpecialAmount());
        }

        return Optional.empty();
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
