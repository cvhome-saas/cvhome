package com.asrevo.cvhome.checkout.service.populator.order;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.asrevo.cvhome.catalog.model.product.ProductDetails;
import com.asrevo.cvhome.catalog.model.product.product.price.FinalPriceCalc;
import com.asrevo.cvhome.catalog.model.product.product.price.SimpleProductPrice;
import com.asrevo.cvhome.catalog.services.product.ExternalProductService;
import com.asrevo.cvhome.checkout.entity.order.orderproduct.OrderProduct;
import com.asrevo.cvhome.checkout.entity.order.orderproduct.OrderProductPrice;
import com.asrevo.cvhome.checkout.entity.shoppingcart.ShoppingCartItem;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.exception.ConversionException;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.store.core.populator.AbstractDataPopulator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Component
@AllArgsConstructor
public class OrderProductPopulator extends AbstractDataPopulator<ShoppingCartItem, StoreMerchantId, OrderProduct> {

    private final ExternalProductService externalProductService;

    @Override
    public OrderProduct populate(ShoppingCartItem source, OrderProduct target, StoreMerchantId store,
                                 LanguageCode language) throws ConversionException {

        try {

            target.setOneTimeCharge(source.getItemPrice());
            target.setProductName("Product " + source.getSku());
            target.setProductQuantity(source.getQuantity());
            target.setSku(source.getSku());

            ProductDetails detailedProduct = externalProductService.getDetailedProduct(store, source.getSku(),
                    language);
            FinalPriceCalc finalPrice = detailedProduct.price();
            if (finalPrice == null) {
                throw new ConversionException("Object final price not populated in shoppingCartItem (source)");
            }
            // Default price
            OrderProductPrice orderProductPrice = orderProductPrice(finalPrice);
            orderProductPrice.setOrderProduct(target);

            Set<OrderProductPrice> prices = new HashSet<>();
            prices.add(orderProductPrice);

            // Other prices
            List<FinalPriceCalc> otherPrices = finalPrice.getAdditionalPrices();
            if (otherPrices != null) {
                for (FinalPriceCalc otherPrice : otherPrices) {
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

    private OrderProductPrice orderProductPrice(FinalPriceCalc price) {

        OrderProductPrice orderProductPrice = new OrderProductPrice();

        SimpleProductPrice productPrice = price.getProductPrice();

        orderProductPrice.setDefaultPrice(productPrice.isDefaultPrice());

        orderProductPrice.setProductPrice(price.getFinalPrice());
        orderProductPrice.setProductPriceCode(productPrice.getCode());
        if (price.isDiscounted()) {
            orderProductPrice.setProductPriceSpecial(productPrice.getProductPriceSpecialAmount());
            orderProductPrice.setProductPriceSpecialStartDate(productPrice.getProductPriceSpecialStartDate());
            orderProductPrice.setProductPriceSpecialEndDate(productPrice.getProductPriceSpecialEndDate());
        }

        return orderProductPrice;
    }

}
