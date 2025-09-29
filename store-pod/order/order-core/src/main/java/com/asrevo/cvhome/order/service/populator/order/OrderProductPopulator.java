package com.asrevo.cvhome.order.service.populator.order;

import com.asrevo.cvhome.catalog.model.product.product.price.FinalPrice;
import com.asrevo.cvhome.catalog.model.product.product.price.SimpleProductPrice;
import com.asrevo.cvhome.catalog.services.product.ExternalProductService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.order.entity.order.orderproduct.OrderProduct;
import com.asrevo.cvhome.order.entity.order.orderproduct.OrderProductPrice;
import com.asrevo.cvhome.order.entity.shoppingcart.ShoppingCartItem;
import com.asrevo.cvhome.store.core.exception.ConversionException;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.core.populator.AbstractDataPopulator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Setter
@Getter
@Component
@AllArgsConstructor
public class OrderProductPopulator
        extends AbstractDataPopulator<ShoppingCartItem, StoreMerchantId, OrderProduct> {
    private final ExternalProductService externalProductService;

    @Override
    public OrderProduct populate(
            ShoppingCartItem source,
            OrderProduct target,
            StoreMerchantId store,
            LanguageCode language)
            throws ConversionException {

        Assert.notNull(externalProductService, "productService must be set");

        try {

            target.setOneTimeCharge(source.getItemPrice());
            target.setProductName("Product " + source.getSku());
            target.setProductQuantity(source.getQuantity());
            target.setSku(source.getSku());

            FinalPrice finalPrice = externalProductService.getProductPrice(store, source.getSku());
            if (finalPrice == null) {
                throw new ConversionException(
                        "Object final price not populated in shoppingCartItem (source)");
            }
            // Default price
            OrderProductPrice orderProductPrice = orderProductPrice(finalPrice);
            orderProductPrice.setOrderProduct(target);

            Set<OrderProductPrice> prices = new HashSet<>();
            prices.add(orderProductPrice);

            // Other prices
            List<FinalPrice> otherPrices = finalPrice.getAdditionalPrices();
            if (otherPrices != null) {
                for (FinalPrice otherPrice : otherPrices) {
                    OrderProductPrice other = orderProductPrice(otherPrice);
                    other.setOrderProduct(target);
                    prices.add(other);
                }
            }

            target.setPrices(prices);

        } catch (Exception e) {
            throw new ConversionException(e);
        }

        return target;
    }

    @Override
    protected OrderProduct createTarget() {
        return null;
    }

    private OrderProductPrice orderProductPrice(FinalPrice price) {

        OrderProductPrice orderProductPrice = new OrderProductPrice();

        SimpleProductPrice productPrice = price.getProductPrice();

        orderProductPrice.setDefaultPrice(productPrice.isDefaultPrice());

        orderProductPrice.setProductPrice(price.getFinalPrice());
        orderProductPrice.setProductPriceCode(productPrice.getCode());
        if (price.isDiscounted()) {
            orderProductPrice.setProductPriceSpecial(productPrice.getProductPriceSpecialAmount());
            orderProductPrice.setProductPriceSpecialStartDate(
                    productPrice.getProductPriceSpecialStartDate());
            orderProductPrice.setProductPriceSpecialEndDate(
                    productPrice.getProductPriceSpecialEndDate());
        }

        return orderProductPrice;
    }
}
