package com.asrevo.cvhome.checkout.services.order;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import org.springframework.data.domain.Page;

import com.asrevo.cvhome.checkout.entity.Customer;
import com.asrevo.cvhome.checkout.entity.Order;
import com.asrevo.cvhome.checkout.entity.OrderLine;
import com.asrevo.cvhome.checkout.entity.OrderLineOption;
import com.asrevo.cvhome.checkout.entity.OrderStatusHistory;
import com.asrevo.cvhome.checkout.entity.OrderTotal;
import com.asrevo.cvhome.checkout.model.order.ReadableOrder;
import com.asrevo.cvhome.checkout.model.order.ReadableOrderConfirmation;
import com.asrevo.cvhome.checkout.model.order.ReadableOrderList;
import com.asrevo.cvhome.checkout.model.order.ReadableOrderProduct;
import com.asrevo.cvhome.checkout.model.order.ReadableOrderProductAttribute;
import com.asrevo.cvhome.checkout.model.order.ReadableOrderStatus;
import com.asrevo.cvhome.checkout.model.order.ReadableOrderStatusHistory;
import com.asrevo.cvhome.checkout.model.order.ReadableOrderTotal;
import com.asrevo.cvhome.checkout.model.order.ReadableTotal;
import com.asrevo.cvhome.checkout.services.customer.CustomerMapper;
import com.asrevo.cvhome.checkout.services.money.MoneyFormatter;

/**
 * Entity → the wire shapes. Money is formatted here, in the store currency for the request locale, because both
 * frontends render {@code price} / {@code subTotal} / {@code total} as-is.
 */
public final class OrderMapper {

    private OrderMapper() {
    }

    public static ReadableOrder toReadable(Order order, Customer customer, boolean detail, Locale locale) {
        ReadableOrder readable = new ReadableOrder();
        readable.setId(order.getId());
        readable.setOrderRef(order.getOrderRef().value());
        readable.setOrderStatus(order.getOrderStatus());
        readable.setPaymentStatus(order.getPaymentStatus());
        readable.setInventoryStatus(order.getInventoryStatus());
        readable.setPaymentType(order.getPaymentType());
        readable.setCurrency(order.getCurrency());
        readable.setDatePurchased(order.getDatePurchased());
        readable.setRedirectUrl(order.getRedirectUrl());
        readable.setNeedsAttention(order.isNeedsAttention());
        readable.setAttentionReason(order.getAttentionReason());
        readable.setComments(order.getComments());
        readable.setBilling(CustomerMapper.toBilling(order.getBilling(), order.getCustomerEmail()));
        readable.setDelivery(CustomerMapper.toDelivery(order.getDelivery()));
        List<ReadableOrderTotal> totals = order.getTotals().stream().map(t -> toTotal(order, t, locale)).toList();
        readable.setTotals(totals);
        readable.setTotal(totals.stream().filter(t -> OrderTotal.TOTAL.equals(t.getCode())).findFirst().orElse(null));
        if (detail) {
            readable.setProducts(order.getLines().stream().map(line -> toProduct(order, line, locale)).toList());
            readable.setCustomer(customer == null ? null : CustomerMapper.toReadable(customer));
        }
        return readable;
    }

    public static ReadableOrderConfirmation toConfirmation(Order order, Locale locale) {
        ReadableOrderConfirmation confirmation = new ReadableOrderConfirmation();
        confirmation.setId(order.getId());
        confirmation.setOrderRef(order.getOrderRef().value());
        confirmation.setBilling(CustomerMapper.toAddress(order.getBilling()));
        confirmation.setDelivery(CustomerMapper.toAddress(order.getDelivery()));
        confirmation.setPayment(order.getPaymentType());
        confirmation.setOrderStatus(order.getOrderStatus());
        confirmation.setPaymentStatus(order.getPaymentStatus());
        confirmation.setRedirectUrl(order.getRedirectUrl());
        confirmation.setDatePurchased(order.getDatePurchased());
        ReadableTotal total = new ReadableTotal();
        total.setValue(order.getTotal());
        total.setTotals(order.getTotals().stream().map(t -> toTotal(order, t, locale)).toList());
        total.setGrandTotal(MoneyFormatter.format(order.getTotal(), order.getCurrency(), locale));
        confirmation.setTotal(total);
        confirmation.setProducts(order.getLines().stream().map(line -> toProduct(order, line, locale)).toList());
        return confirmation;
    }

    public static ReadableOrderStatus toStatus(Order order) {
        ReadableOrderStatus status = new ReadableOrderStatus();
        status.setOrderId(order.getId());
        status.setOrderStatus(order.getOrderStatus());
        status.setPaymentStatus(order.getPaymentStatus());
        status.setRedirectUrl(order.isAwaitingPayment() ? order.getRedirectUrl() : null);
        return status;
    }

    public static List<ReadableOrderStatusHistory> toHistory(Order order) {
        return order.getHistory().stream().map(entry -> toHistory(order, entry)).toList();
    }

    public static ReadableOrderStatusHistory toHistory(Order order, OrderStatusHistory entry) {
        ReadableOrderStatusHistory readable = new ReadableOrderStatusHistory();
        readable.setId(entry.getId());
        readable.setOrderId(order.getId());
        readable.setOrderStatus(entry.getStatus());
        readable.setComments(entry.getComments());
        readable.setDate(entry.getDateAdded());
        return readable;
    }

    public static ReadableOrderList toList(Page<Order> page, Function<Order, ReadableOrder> convert) {
        ReadableOrderList list = new ReadableOrderList();
        list.setContent(page.getContent().stream().map(convert).toList());
        list.setSize(page.getNumberOfElements());
        list.setTotalElements(page.getTotalElements());
        list.setTotalPages(page.getTotalPages());
        list.setPageNumber(page.getNumber());
        list.setRecordsFiltered(page.getNumberOfElements());
        return list;
    }

    private static ReadableOrderTotal toTotal(Order order, OrderTotal total, Locale locale) {
        ReadableOrderTotal readable = new ReadableOrderTotal();
        readable.setId(total.getId());
        readable.setCode(total.getCode());
        readable.setModule(total.getModule());
        readable.setTitle(total.getTitle());
        readable.setOrder(total.getSortOrder());
        readable.setValue(total.getValue());
        readable.setTotal(MoneyFormatter.format(total.getValue(), order.getCurrency(), locale));
        return readable;
    }

    private static ReadableOrderProduct toProduct(Order order, OrderLine line, Locale locale) {
        ReadableOrderProduct product = new ReadableOrderProduct();
        product.setId(line.getId());
        product.setSku(line.getSku());
        product.setProductName(line.getProductName());
        product.setOrderedQuantity(line.getQuantity());
        product.setPrice(MoneyFormatter.format(line.getUnitPrice(), order.getCurrency(), locale));
        product.setSubTotal(MoneyFormatter.format(line.getLineTotal(), order.getCurrency(), locale));
        product.setImage(line.getImageUrl());
        product.setAttributes(line.getOptions().isEmpty() ? null
                : line.getOptions().stream().map(OrderMapper::toAttribute).toList());
        return product;
    }

    private static ReadableOrderProductAttribute toAttribute(OrderLineOption option) {
        ReadableOrderProductAttribute attribute = new ReadableOrderProductAttribute();
        attribute.setId(option.getId());
        attribute.setAttributeName(option.getOptionName());
        attribute.setAttributeValue(option.getValueName());
        return attribute;
    }
}
