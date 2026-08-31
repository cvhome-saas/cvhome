package com.asrevo.cvhome.checkout.service.mapper.order;

import java.util.HashSet;
import java.util.Set;

import com.asrevo.cvhome.catalog.model.product.ReadableVariantOptionValue;
import com.asrevo.cvhome.checkout.entity.order.orderproduct.OrderProduct;
import com.asrevo.cvhome.checkout.entity.order.orderproduct.OrderProductOption;
import com.asrevo.cvhome.checkout.entity.order.orderproduct.OrderProductPrice;
import com.asrevo.cvhome.checkout.entity.shoppingcart.ShoppingCartItem;
import com.asrevo.cvhome.checkout.errors.OrderProductPriceMissingException;
import com.asrevo.cvhome.checkout.model.product.ProductDetails;
import com.asrevo.cvhome.inventory.model.SkuPrice;

/**
 * Builds the order line an accepted cart line becomes — fed the already-composed {@link ProductDetails}, so
 * placement makes exactly two s2s calls for the whole order, never one pair per line. Everything a rendered
 * order needs is copied here: the real localized product name, the price rows, and the sold variant's
 * option/value labels — an order must survive any later catalog edit.
 */
public final class OrderLineMapper {

    /**
     * Inventory's single price code; the order keeps it so historic orders read like pre-split ones.
     */
    private static final String DEFAULT_PRICE_CODE = "base";

    private static final int NAME_LIMIT = 255;

    private OrderLineMapper() {
    }

    /**
     * @param details the composed product+inventory for the line's sku, or null when the catalog no longer
     *                knows it — the line still books under its sku
     */
    public static OrderProduct toOrderProduct(ShoppingCartItem source, ProductDetails details)
            throws OrderProductPriceMissingException {
        OrderProduct target = new OrderProduct();
        target.setOneTimeCharge(source.getItemPrice());
        target.setProductName(productName(source, details));
        target.setProductQuantity(source.getQuantity());
        target.setSku(source.getSku());
        target.setPrices(prices(source, details, target));
        target.setOrderOptions(optionSnapshot(details, target));
        return target;
    }

    /**
     * The catalog copy in the shopper's language; the sku is the honest fallback when the product (or its copy)
     * is gone.
     */
    private static String productName(ShoppingCartItem source, ProductDetails details) {
        String name = details == null || details.product() == null || details.product().getDescription() == null
                ? null
                : details.product().getDescription().getName();
        if (name == null || name.isBlank()) {
            name = source.getSku();
        }
        return name.length() > NAME_LIMIT ? name.substring(0, NAME_LIMIT) : name;
    }

    private static Set<OrderProductPrice> prices(ShoppingCartItem source, ProductDetails details,
                                                 OrderProduct target) throws OrderProductPriceMissingException {
        SkuPrice price = details == null ? null : details.inventory().price();
        if (price == null) {
            throw OrderProductPriceMissingException.of(source.getSku());
        }
        OrderProductPrice orderProductPrice = new OrderProductPrice();
        orderProductPrice.setDefaultPrice(true);
        orderProductPrice.setProductPriceCode(DEFAULT_PRICE_CODE);
        orderProductPrice.setProductPrice(price.finalPrice());
        if (price.discounted()) {
            orderProductPrice.setProductPriceSpecial(price.specialAmount());
            orderProductPrice.setProductPriceSpecialStartDate(price.specialStartDate());
            orderProductPrice.setProductPriceSpecialEndDate(price.specialEndDate());
        }
        orderProductPrice.setOrderProduct(target);
        Set<OrderProductPrice> prices = new HashSet<>();
        prices.add(orderProductPrice);
        return prices;
    }

    /**
     * The sold variant's labels, copied — not referenced — so "Color: Red / Size: L" still renders when the
     * option or the product is edited or deleted later. Empty for a default-variant line.
     */
    private static Set<OrderProductOption> optionSnapshot(ProductDetails details, OrderProduct target) {
        Set<OrderProductOption> options = new HashSet<>();
        if (details == null || details.product() == null || details.product().getVariant() == null) {
            return options;
        }
        for (ReadableVariantOptionValue value : details.product().getVariant().getOptionValues()) {
            OrderProductOption option = new OrderProductOption();
            option.setOrderProduct(target);
            option.setOptionCode(value.getOptionCode());
            option.setOptionName(value.getOptionName() == null ? value.getOptionCode() : value.getOptionName());
            option.setValueCode(value.getValueCode());
            option.setValueName(value.getValueName() == null ? value.getValueCode() : value.getValueName());
            option.setSortOrder(value.getSortOrder());
            options.add(option);
        }
        return options;
    }
}
