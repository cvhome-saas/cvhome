package com.asrevo.cvhome.checkout.service.populator.order;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.asrevo.cvhome.checkout.entity.order.Order;
import com.asrevo.cvhome.checkout.entity.order.OrderTotal;
import com.asrevo.cvhome.checkout.entity.order.OrderTotalType;
import com.asrevo.cvhome.checkout.model.order.v0.ReadableOrder;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.customer.model.customer.ReadableBilling;
import com.asrevo.cvhome.customer.model.customer.ReadableDelivery;
import com.asrevo.cvhome.store.core.populator.AbstractDataPopulator;

@Component
public class ReadableOrderPopulator extends AbstractDataPopulator<Order, StoreMerchantId, ReadableOrder> {

    @Override
    public ReadableOrder populate(Order source, ReadableOrder target, StoreMerchantId store, LanguageCode language) {

        target.setId(source.getId());
        target.setDatePurchased(source.getDatePurchased());
        target.setOrderStatus(source.getStatus());
        target.setCurrency(source.getCurrency());
        target.setPaymentStatus(source.getPaymentStatus());
        target.setReservationStatus(source.getInventoryStatus());
        target.setRedirectUri(source.getRedirectUri());

        target.setStore(source.getStoreMerchantId());

        if (source.getCustomerAgreement() != null) {
            target.setCustomerAgreed(source.getCustomerAgreement());
        }
        if (source.getConfirmedAddress() != null) {
            target.setConfirmedAddress(source.getConfirmedAddress());
        }

        applyBilling(source, target);
        applyDelivery(source, target);
        applyTotals(source, target);

        return target;
    }

    private void applyBilling(Order source, ReadableOrder target) {
        if (source.getBilling() == null) {
            return;
        }
        ReadableBilling address = new ReadableBilling();
        address.setEmail(source.getCustomerEmailAddress());
        address.setCity(source.getBilling().getCity());
        address.setAddress(source.getBilling().getAddress());
        address.setCompany(source.getBilling().getCompany());
        address.setFirstName(source.getBilling().getFirstName());
        address.setLastName(source.getBilling().getLastName());
        address.setPostalCode(source.getBilling().getPostalCode());
        address.setPhone(source.getBilling().getTelephone());
        if (source.getBilling().getCountry() != null) {
            address.setCountry(source.getBilling().getCountry());
        }
        if (source.getBilling().getZone() != null) {
            address.setZone(source.getBilling().getZone());
        }

        target.setBilling(address);
    }

    private void applyDelivery(Order source, ReadableOrder target) {
        if (source.getDelivery() == null) {
            return;
        }
        ReadableDelivery address = new ReadableDelivery();
        address.setCity(source.getDelivery().getCity());
        address.setAddress(source.getDelivery().getAddress());
        address.setCompany(source.getDelivery().getCompany());
        address.setFirstName(source.getDelivery().getFirstName());
        address.setLastName(source.getDelivery().getLastName());
        address.setPostalCode(source.getDelivery().getPostalCode());
        address.setPhone(source.getDelivery().getTelephone());
        if (source.getDelivery().getCountry() != null) {
            address.setCountry(source.getDelivery().getCountry());
        }
        if (source.getDelivery().getZone() != null) {
            address.setZone(source.getDelivery().getZone());
        }

        target.setDelivery(address);
    }

    private void applyTotals(Order source, ReadableOrder target) {
        com.asrevo.cvhome.checkout.model.order.total.OrderTotal taxTotal = null;
        com.asrevo.cvhome.checkout.model.order.total.OrderTotal shippingTotal = null;

        List<com.asrevo.cvhome.checkout.model.order.total.OrderTotal> totals = new ArrayList<>();
        for (OrderTotal t : source.getOrderTotal()) {
            if (t.getOrderTotalType() == null) {
                continue;
            }
            if (t.getOrderTotalType().name().equals(OrderTotalType.TOTAL.name())) {
                com.asrevo.cvhome.checkout.model.order.total.OrderTotal totalTotal = createTotal(t);
                target.setTotal(totalTotal);
                totals.add(totalTotal);
            } else if (t.getOrderTotalType().name().equals(OrderTotalType.TAX.name())) {
                com.asrevo.cvhome.checkout.model.order.total.OrderTotal totalTotal = createTotal(t);
                taxTotal = accumulate(taxTotal, totalTotal);
                target.setTax(totalTotal);
                totals.add(totalTotal);
            } else if (t.getOrderTotalType().name().equals(OrderTotalType.SHIPPING.name())
                    || t.getOrderTotalType().name().equals(OrderTotalType.HANDLING.name())) {
                com.asrevo.cvhome.checkout.model.order.total.OrderTotal totalTotal = createTotal(t);
                shippingTotal = accumulate(shippingTotal, totalTotal);
                target.setShipping(totalTotal);
                totals.add(totalTotal);
            } else if (t.getOrderTotalType().name().equals(OrderTotalType.SUBTOTAL.name())) {
                com.asrevo.cvhome.checkout.model.order.total.OrderTotal subTotal = createTotal(t);
                totals.add(subTotal);

            } else {
                com.asrevo.cvhome.checkout.model.order.total.OrderTotal otherTotal = createTotal(t);
                totals.add(otherTotal);
            }
        }

        target.setTotals(totals);
    }

    private com.asrevo.cvhome.checkout.model.order.total.OrderTotal accumulate(
            com.asrevo.cvhome.checkout.model.order.total.OrderTotal existing,
            com.asrevo.cvhome.checkout.model.order.total.OrderTotal totalTotal) {
        if (existing == null) {
            return totalTotal;
        }
        BigDecimal v = existing.getValue();
        v = v.add(totalTotal.getValue());
        existing.setValue(v);
        return existing;
    }

    private com.asrevo.cvhome.checkout.model.order.total.OrderTotal createTotal(OrderTotal t) {
        com.asrevo.cvhome.checkout.model.order.total.OrderTotal totalTotal = new com.asrevo.cvhome.checkout.model.order.total.OrderTotal();
        totalTotal.setCode(t.getOrderTotalCode());
        totalTotal.setId(t.getId());
        totalTotal.setModule(t.getModule());
        totalTotal.setOrder(t.getSortOrder());
        totalTotal.setValue(t.getValue());
        return totalTotal;
    }

    @Override
    protected ReadableOrder createTarget() {

        return null;
    }

}
