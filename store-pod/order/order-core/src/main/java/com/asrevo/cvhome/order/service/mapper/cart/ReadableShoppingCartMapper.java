package com.asrevo.cvhome.order.service.mapper.cart;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.order.entity.order.OrderTotal;
import com.asrevo.cvhome.order.entity.order.OrderTotalSummary;
import com.asrevo.cvhome.order.entity.shoppingcart.ShoppingCart;
import com.asrevo.cvhome.order.entity.shoppingcart.ShoppingCartItem;
import com.asrevo.cvhome.order.model.order.total.ReadableOrderTotal;
import com.asrevo.cvhome.order.model.shoppingcart.ReadableShoppingCart;
import com.asrevo.cvhome.order.model.shoppingcart.ReadableShoppingCartItem;
import com.asrevo.cvhome.order.services.shoppingcart.ShoppingCartCalculationService;
import com.asrevo.cvhome.store.controller.exception.ConversionRuntimeException;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.mapper.Mapper;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.utils.PriceUtils;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
@Slf4j
public class ReadableShoppingCartMapper implements Mapper<ShoppingCart, ReadableShoppingCart> {

    private final ShoppingCartCalculationService shoppingCartCalculationService;

    /*
        private final ProductVariantService productVariantService;

        private final ReadableProductVariationMapper readableProductVariationMapper;

        private final ImageFilePath imageUtils;
    */

    private final MinimalProductToReadableShoppingCartItem minimalProductToReadableShoppingCartItem;
    private final ExternalMerchantStoreService externalMerchantStoreService;

    public ReadableShoppingCartMapper(
            ShoppingCartCalculationService shoppingCartCalculationService,
            MinimalProductToReadableShoppingCartItem minimalProductToReadableShoppingCartItem,
            ExternalMerchantStoreService externalMerchantStoreService) {
        this.shoppingCartCalculationService = shoppingCartCalculationService;
        this.minimalProductToReadableShoppingCartItem = minimalProductToReadableShoppingCartItem;
        this.externalMerchantStoreService = externalMerchantStoreService;
    }

    @Override
    public ReadableShoppingCart convert(
            ShoppingCart source, StoreMerchantId store, LanguageCode language) {
        ReadableShoppingCart destination = new ReadableShoppingCart();
        return this.merge(source, destination, store, language);
    }

    /*
        private ReadableImage image(ProductVariantImage instanceImage, StoreMerchantId store, LanguageCode language) {
            ReadableImage img = new ReadableImage();
            img.setDefaultImage(instanceImage.isDefaultImage());
            img.setId(instanceImage.getId());
            img.setImageName(instanceImage.getProductImage());
            img.setImageUrl(imageUtils.buildCustomTypeImageUtils(store, img.getImageName(), FileContentType.VARIANT));
            return img;
        }
    */

    @Override
    public ReadableShoppingCart merge(
            ShoppingCart source,
            ReadableShoppingCart destination,
            StoreMerchantId store,
            LanguageCode language) {
        Assert.notNull(source, "ShoppingCart cannot be null");
        Assert.notNull(destination, "ReadableShoppingCart cannot be null");
        Assert.notNull(store, "store cannot be null");
        Assert.notNull(language, "Language cannot be null");

        destination.setCode(source.getShoppingCartCode());
        int cartQuantity = 0;

        destination.setCustomer(source.getCustomerId());

        try {

            if (!StringUtils.isBlank(source.getPromoCode())) {
                Date promoDateAdded = source.getPromoAdded(); // promo valid 1 day
                if (promoDateAdded == null) {
                    promoDateAdded = new Date();
                }
                Instant instant = promoDateAdded.toInstant();
                ZonedDateTime zdt = instant.atZone(ZoneId.systemDefault());
                LocalDate date = zdt.toLocalDate();
                // date added < date + 1 day
                LocalDate tomorrow = LocalDate.now().plusDays(1);
                if (date.isBefore(tomorrow)) {
                    destination.setPromoCode(source.getPromoCode());
                }
            }

            Set<ShoppingCartItem> items = source.getLineItems();

            if (items != null) {

                for (ShoppingCartItem item : items) {
                    ReadableShoppingCartItem shoppingCartItem =
                            minimalProductToReadableShoppingCartItem.convert(item, store, language);

                    // variation
                    /*
                                        if (item.getVariant() != null) {
                                            ProductVariant productVariant = productVariantService.getById(item.getVariant(), store)
                                                    .orElseThrow(() -> new ConversionRuntimeException("An error occured during shopping cart [" + source.getShoppingCartCode() + "] conversion, productVariant [" + item.getVariant() + "] not found"));

                                            shoppingCartItem.setVariant(readableProductVariationMapper.convert(productVariant.getVariation(), store, language));
                                            if (productVariant.getVariationValue() != null) {
                                                shoppingCartItem.setVariantValue(readableProductVariationMapper.convert(productVariant.getVariationValue(), store, language));
                                            }

                                            if (productVariant.getProductVariantGroup() != null) {
                                                Set<String> nameSet = new HashSet<>();
                                                List<ReadableImage> instanceImages = productVariant.getProductVariantGroup().getImages()
                                                        .stream().map(i -> this.image(i, store, language))
                                                        .filter(e -> nameSet.add(e.getImageUrl()))
                                                        .collect(Collectors.toList());
                                                shoppingCartItem.setImages(instanceImages);
                                            }
                                        }
                    */

                    shoppingCartItem.setPrice(item.getItemPrice());
                    shoppingCartItem.setFinalPrice(
                            PriceUtils.getStoreFormatedAmountWithCurrency(
                                    externalMerchantStoreService.getStore(store),
                                    item.getItemPrice()));

                    shoppingCartItem.setQuantity(item.getQuantity());

                    cartQuantity = cartQuantity + item.getQuantity();

                    BigDecimal subTotal =
                            PriceUtils.calculatePriceQuantity(
                                    item.getItemPrice(), item.getQuantity());

                    // calculate sub total (price * quantity)
                    shoppingCartItem.setSubTotal(subTotal);

                    shoppingCartItem.setDisplaySubTotal(
                            PriceUtils.getStoreFormatedAmountWithCurrency(
                                    externalMerchantStoreService.getStore(store), subTotal));
                    destination.getProducts().add(shoppingCartItem);
                }
            }

            // OrdetTotalSummary contains all calculations

            OrderTotalSummary orderSummary =
                    shoppingCartCalculationService.calculate(source, store, language);

            if (CollectionUtils.isNotEmpty(orderSummary.getTotals())) {

                if (orderSummary.getTotals().stream()
                        .noneMatch(
                                t -> Constants.OT_DISCOUNT_TITLE.equals(t.getOrderTotalCode()))) {
                    // no promo coupon applied
                    destination.setPromoCode(null);
                }

                List<ReadableOrderTotal> totals = new ArrayList<>();
                for (OrderTotal t : orderSummary.getTotals()) {
                    ReadableOrderTotal total = new ReadableOrderTotal();
                    total.setCode(t.getOrderTotalCode());
                    total.setValue(t.getValue());
                    total.setText(t.getText());
                    totals.add(total);
                }
                destination.setTotals(totals);
            }

            destination.setSubtotal(orderSummary.getSubTotal());
            destination.setDisplaySubTotal(
                    PriceUtils.getStoreFormatedAmountWithCurrency(
                            externalMerchantStoreService.getStore(store),
                            orderSummary.getSubTotal()));

            destination.setTotal(orderSummary.getTotal());
            destination.setDisplayTotal(
                    PriceUtils.getStoreFormatedAmountWithCurrency(
                            externalMerchantStoreService.getStore(store), orderSummary.getTotal()));

            destination.setQuantity(cartQuantity);
            destination.setId(source.getId());

            if (source.getOrderId() != null) {
                destination.setOrder(source.getOrderId());
            }

        } catch (Exception e) {
            throw new ConversionRuntimeException(
                    "An error occurred while converting ReadableShoppingCart", e);
        }

        return destination;
    }
}
