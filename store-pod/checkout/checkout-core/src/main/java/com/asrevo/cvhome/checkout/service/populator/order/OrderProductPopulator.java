package com.asrevo.cvhome.checkout.service.populator.order;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.asrevo.cvhome.checkout.entity.order.orderproduct.OrderProduct;
import com.asrevo.cvhome.checkout.entity.order.orderproduct.OrderProductPrice;
import com.asrevo.cvhome.checkout.entity.shoppingcart.ShoppingCartItem;
import com.asrevo.cvhome.checkout.errors.OrderProductNotConvertibleException;
import com.asrevo.cvhome.checkout.errors.OrderProductPriceMissingException;
import com.asrevo.cvhome.checkout.model.product.ProductDetails;
import com.asrevo.cvhome.checkout.service.facade.product.ProductDetailsComposer;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.inventory.model.SkuPrice;
import com.asrevo.cvhome.store.core.populator.AbstractDataPopulator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Component
@AllArgsConstructor
public class OrderProductPopulator extends AbstractDataPopulator<ShoppingCartItem, StoreMerchantId, OrderProduct> {

    /**
     * Inventory's single price code; the order keeps it so historic orders read like pre-split ones.
     */
    private static final String DEFAULT_PRICE_CODE = "base";

    private final ProductDetailsComposer productDetailsComposer;

    @Override
    public OrderProduct populate(ShoppingCartItem source, OrderProduct target, StoreMerchantId store,
                                 LanguageCode language)
            throws OrderProductPriceMissingException, OrderProductNotConvertibleException {

        try {

            target.setOneTimeCharge(source.getItemPrice());
            target.setProductName(String.format("Product %s", source.getSku()));
            target.setProductQuantity(source.getQuantity());
            target.setSku(source.getSku());

            ProductDetails detailedProduct = productDetailsComposer.getDetailedProduct(store, source.getSku(),
                    language);
            SkuPrice price = detailedProduct.inventory().price();
            if (price == null) {
                throw OrderProductPriceMissingException.of(source.getSku());
            }
            OrderProductPrice orderProductPrice = orderProductPrice(price);
            orderProductPrice.setOrderProduct(target);

            Set<OrderProductPrice> prices = new HashSet<>();
            prices.add(orderProductPrice);
            target.setPrices(prices);

        } catch (OrderProductPriceMissingException e) {
            // Already names its condition; re-wrapping it would bury the sku the caller needs.
            throw e;
        } catch (Exception e) {
            throw OrderProductNotConvertibleException.of(source.getSku(), e);
        }

        return target;
    }

    @Override
    protected OrderProduct createTarget() {
        return null;
    }

    /**
     * The one price the single-product model sells at, recorded as the order line's default price.
     */
    private OrderProductPrice orderProductPrice(SkuPrice price) {
        OrderProductPrice orderProductPrice = new OrderProductPrice();
        orderProductPrice.setDefaultPrice(true);
        orderProductPrice.setProductPriceCode(DEFAULT_PRICE_CODE);
        orderProductPrice.setProductPrice(price.finalPrice());
        if (price.discounted()) {
            orderProductPrice.setProductPriceSpecial(price.specialAmount());
            orderProductPrice.setProductPriceSpecialStartDate(price.specialStartDate());
            orderProductPrice.setProductPriceSpecialEndDate(price.specialEndDate());
        }
        return orderProductPrice;
    }

}
