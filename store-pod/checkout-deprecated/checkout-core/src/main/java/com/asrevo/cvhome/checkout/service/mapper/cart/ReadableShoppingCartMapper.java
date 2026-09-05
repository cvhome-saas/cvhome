package com.asrevo.cvhome.checkout.service.mapper.cart;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.asrevo.cvhome.catalog.model.product.ReadableMinimalProduct;
import com.asrevo.cvhome.checkout.entity.order.OrderSummary;
import com.asrevo.cvhome.checkout.entity.order.OrderTotal;
import com.asrevo.cvhome.checkout.entity.order.OrderTotalSummary;
import com.asrevo.cvhome.checkout.entity.shoppingcart.ShoppingCart;
import com.asrevo.cvhome.checkout.entity.shoppingcart.ShoppingCartItem;
import com.asrevo.cvhome.checkout.model.order.total.ReadableOrderTotal;
import com.asrevo.cvhome.checkout.model.product.ProductDetails;
import com.asrevo.cvhome.checkout.model.shoppingcart.ReadableShoppingCart;
import com.asrevo.cvhome.checkout.model.shoppingcart.ReadableShoppingCartItem;
import com.asrevo.cvhome.checkout.service.facade.product.ProductDetailsComposer;
import com.asrevo.cvhome.checkout.services.order.OrderService;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.merchant.model.merchant.ReadableMerchantStore;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.mapper.Mapper;
import com.asrevo.cvhome.store.utils.PriceUtils;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ReadableShoppingCartMapper implements Mapper<ShoppingCart, ReadableShoppingCart> {

    private final OrderService orderService;

    private final ExternalMerchantStoreService externalMerchantStoreService;

    private final ProductDetailsComposer productDetailsComposer;

    public ReadableShoppingCartMapper(
            ExternalMerchantStoreService externalMerchantStoreService,
            ProductDetailsComposer productDetailsComposer, OrderService orderService) {
        this.externalMerchantStoreService = externalMerchantStoreService;
        this.productDetailsComposer = productDetailsComposer;
        this.orderService = orderService;
    }

    @Override
    public ReadableShoppingCart convert(ShoppingCart source, StoreMerchantId store, LanguageCode language) {
        ReadableShoppingCart destination = new ReadableShoppingCart();
        return this.merge(source, destination, store, language);
    }

    @Override
    public ReadableShoppingCart merge(ShoppingCart source, ReadableShoppingCart destination, StoreMerchantId store,
                                      LanguageCode language) {
        destination.setCode(source.getShoppingCartCode());
        destination.setCustomer(source.getCustomerId());

        applyPromoCode(source, destination);

        ReadableMerchantStore merchantStore = externalMerchantStoreService.getStore(store);
        int cartQuantity = applyItems(source, destination, store, language, merchantStore);

        applyTotals(source, destination, store, merchantStore);

        destination.setQuantity(cartQuantity);
        destination.setId(source.getId());

        if (source.getOrderId() != null) {
            destination.setOrder(source.getOrderId());
        }

        return destination;
    }

    private void applyPromoCode(ShoppingCart source, ReadableShoppingCart destination) {
        if (StringUtils.isBlank(source.getPromoCode())) {
            return;
        }
        Instant promoDateAdded = source.getPromoAdded(); // promo valid 1 day
        if (promoDateAdded == null) {
            promoDateAdded = Instant.now();
        }
        ZonedDateTime zdt = promoDateAdded.atZone(ZoneId.systemDefault());
        LocalDate date = zdt.toLocalDate();
        // date added < date + 1 day
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        if (date.isBefore(tomorrow)) {
            destination.setPromoCode(source.getPromoCode());
        }
    }

    private int applyItems(ShoppingCart source, ReadableShoppingCart destination, StoreMerchantId store,
            LanguageCode language, ReadableMerchantStore merchantStore) {
        int cartQuantity = 0;
        Set<ShoppingCartItem> items = Optional.ofNullable(source.getLineItems()).orElse(Set.of());

        // one catalog call + one inventory call for the whole cart, never one pair per line
        Map<String, ProductDetails> detailsBySku = productDetailsComposer.getDetailedProducts(store,
                items.stream().map(ShoppingCartItem::getSku).toList(), language);

        for (ShoppingCartItem item : items) {
            ProductDetails detailedProduct = detailsBySku.get(item.getSku());
            ReadableMinimalProduct minimalProduct = detailedProduct == null ? null : detailedProduct.product();
            if (minimalProduct == null) {
                continue;
            }
            ReadableShoppingCartItem shoppingCartItem = new ReadableShoppingCartItem();
            copyProductProperties(shoppingCartItem, minimalProduct);

            shoppingCartItem.setPrice(item.getItemPrice());
            shoppingCartItem.setFinalPrice(
                    PriceUtils.getStoreFormatedAmountWithCurrency(merchantStore, item.getItemPrice()));

            shoppingCartItem.setQuantity(item.getQuantity());

            cartQuantity = cartQuantity + item.getQuantity();

            BigDecimal subTotal = PriceUtils.calculatePriceQuantity(item.getItemPrice(), item.getQuantity());

            // calculate sub total (price * quantity)
            shoppingCartItem.setSubTotal(subTotal);

            shoppingCartItem.setDisplaySubTotal(PriceUtils.getStoreFormatedAmountWithCurrency(merchantStore, subTotal));
            destination.getProducts().add(shoppingCartItem);
        }
        return cartQuantity;
    }

    /**
     * Reflectively copies the product's properties onto the cart line.
     *
     * <p>
     * The two checked exceptions {@code BeanUtils} declares mean our own DTOs cannot be introspected — a bug in this
     * code, not a condition a caller can act on, so it gets no named type and falls to the advice's internal-error
     * fallback: a 500 with a {@code traceId} that leads to the stack trace. It used to become
     * {@code ConversionRuntimeException}, reporting {@code LEGACY.CONVERSION} and a 400, which blamed the caller for
     * it.
     * </p>
     */
    private void copyProductProperties(ReadableShoppingCartItem target, ReadableMinimalProduct source) {
        try {
            BeanUtils.copyProperties(target, source);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Could not copy product properties onto the cart line", e);
        }
    }

    private void applyTotals(ShoppingCart source, ReadableShoppingCart destination, StoreMerchantId store,
            ReadableMerchantStore merchantStore) {
        // OrdetTotalSummary contains all calculations

        OrderSummary summary = new OrderSummary();
        summary.setProducts(new ArrayList<>(source.getLineItems()));

        OrderTotalSummary orderSummary = orderService.calculateOrderTotal(summary, store);

        if (CollectionUtils.isNotEmpty(orderSummary.getTotals())) {

            if (orderSummary.getTotals()
                    .stream()
                    .noneMatch(t -> Constants.OT_DISCOUNT_TITLE.equals(t.getOrderTotalCode()))) {
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
                PriceUtils.getStoreFormatedAmountWithCurrency(merchantStore, orderSummary.getSubTotal()));

        destination.setTotal(orderSummary.getTotal());
        destination
                .setDisplayTotal(PriceUtils.getStoreFormatedAmountWithCurrency(merchantStore, orderSummary.getTotal()));
    }

}
