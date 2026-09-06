package com.asrevo.cvhome.checkout.services.cart;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.asrevo.cvhome.catalog.model.product.ReadableMinimalProduct;
import com.asrevo.cvhome.checkout.entity.Cart;
import com.asrevo.cvhome.checkout.entity.CartLine;
import com.asrevo.cvhome.checkout.entity.OrderTotal;
import com.asrevo.cvhome.checkout.model.cart.ReadableCart;
import com.asrevo.cvhome.checkout.model.cart.ReadableCartItem;
import com.asrevo.cvhome.checkout.model.order.ReadableOrderTotal;
import com.asrevo.cvhome.checkout.services.catalog.ProductSnapshot;
import com.asrevo.cvhome.checkout.services.money.MoneyFormatter;
import com.asrevo.cvhome.commons.domain.CurrencyCode;

/**
 * Entity + live snapshot → the cart the storefront renders. A line whose sku is missing from the snapshot is left out.
 */
public final class CartMapper {

    private CartMapper() {
    }

    public static ReadableCart toReadable(Cart cart, Map<String, ProductSnapshot> snapshot, CurrencyCode currency,
                                          Locale locale) {
        ReadableCart readable = new ReadableCart();
        readable.setId(cart.getId());
        readable.setCode(cart.getCode().value());
        readable.setLanguage(cart.getLanguage() == null ? null : cart.getLanguage().code());
        readable.setOrder(cart.getOrderId());

        List<ReadableCartItem> items = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        int quantity = 0;
        for (CartLine line : cart.getLines()) {
            ProductSnapshot product = snapshot.get(line.getSku());
            if (product == null) {
                continue;
            }
            ReadableCartItem item = toItem(line, product, currency, locale);
            items.add(item);
            subtotal = subtotal.add(item.getSubTotal());
            quantity += line.getQuantity();
        }
        readable.setProducts(items);
        readable.setQuantity(quantity);
        readable.setSubtotal(subtotal);
        readable.setDisplaySubTotal(MoneyFormatter.format(subtotal, currency, locale));
        readable.setTotal(subtotal);
        readable.setDisplayTotal(readable.getDisplaySubTotal());
        readable.setTotals(List.of(total(OrderTotal.SUBTOTAL, "summary", 0, subtotal, currency, locale),
                total(OrderTotal.TOTAL, "total", 1, subtotal, currency, locale)));
        return readable;
    }

    private static ReadableCartItem toItem(CartLine line, ProductSnapshot product, CurrencyCode currency,
                                           Locale locale) {
        ReadableCartItem item = new ReadableCartItem();
        copyProduct(product.product(), item);
        item.setAvailable(product.canBePurchased());
        item.setQuantity(line.getQuantity());
        item.setPrice(product.finalPrice());
        item.setFinalPrice(MoneyFormatter.format(product.finalPrice(), currency, locale));
        item.setSubTotal(product.finalPrice().multiply(BigDecimal.valueOf(line.getQuantity())));
        item.setDisplaySubTotal(MoneyFormatter.format(item.getSubTotal(), currency, locale));
        item.setQuantityOrderMinimum(product.quantityOrderMinimum());
        item.setQuantityOrderMaximum(product.quantityOrderMaximum());
        return item;
    }

    private static void copyProduct(ReadableMinimalProduct source, ReadableCartItem target) {
        target.setId(source.getId());
        target.setSku(source.getSku());
        target.setVariantCount(source.getVariantCount());
        target.setVariant(source.getVariant());
        target.setProductShipeable(source.isProductShipeable());
        target.setProductVirtual(source.isProductVirtual());
        target.setSortOrder(source.getSortOrder());
        target.setDateAvailable(source.getDateAvailable());
        target.setProductSpecifications(source.getProductSpecifications());
        target.setDescription(source.getDescription());
        target.setImage(source.getImage());
        target.setImages(source.getImages());
    }

    public static ReadableOrderTotal total(String code, String module, int order, BigDecimal value,
                                           CurrencyCode currency, Locale locale) {
        ReadableOrderTotal total = new ReadableOrderTotal();
        total.setCode(code);
        total.setModule(module);
        total.setOrder(order);
        total.setValue(value);
        total.setTotal(MoneyFormatter.format(value, currency, locale));
        return total;
    }
}
