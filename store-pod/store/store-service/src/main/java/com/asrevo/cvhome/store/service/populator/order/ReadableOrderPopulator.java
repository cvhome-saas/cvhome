package com.asrevo.cvhome.store.service.populator.order;

import com.asrevo.cvhome.store.controller.exception.ConversionException;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.order.Order;
import com.asrevo.cvhome.store.core.entity.order.OrderTotal;
import com.asrevo.cvhome.store.core.entity.order.OrderTotalType;
import com.asrevo.cvhome.store.core.entity.order.attributes.OrderAttribute;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.customer.ReadableBilling;
import com.asrevo.cvhome.store.core.model.customer.ReadableDelivery;
import com.asrevo.cvhome.store.core.model.order.v0.ReadableOrder;
import com.asrevo.cvhome.store.core.model.store.ReadableMerchantStore;
import com.asrevo.cvhome.store.service.populator.store.ReadableMerchantStorePopulator;
import com.asrevo.cvhome.store.utils.AbstractDataPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class ReadableOrderPopulator extends
        AbstractDataPopulator<Order, ReadableOrder> {

    @Autowired
    private ReadableMerchantStorePopulator readableMerchantStorePopulator;


    @Override
    public ReadableOrder populate(Order source, ReadableOrder target,
                                  MerchantStore store, Language language) throws ConversionException {


        target.setId(source.getId());
        target.setDatePurchased(source.getDatePurchased());
        target.setOrderStatus(source.getStatus());
        target.setCurrency(source.getCurrency().getCode());
        //target.setCurrencyModel(source.getCurrency());

        target.setPaymentType(source.getPaymentType());
        target.setPaymentModule(source.getPaymentModuleCode());
        target.setShippingModule(source.getShippingModuleCode());

        if (source.getMerchant() != null) {
/*			ReadableMerchantStorePopulator merchantPopulator = new ReadableMerchantStorePopulator();
			merchantPopulator.setCountryService(countryService);
			merchantPopulator.setFilePath(filePath);
			merchantPopulator.setZoneService(zoneService);*/
            ReadableMerchantStore readableStore =
                    readableMerchantStorePopulator.populate(source.getMerchant(), null, store, source.getMerchant().getDefaultLanguage());
            target.setStore(readableStore);
        }


        if (source.getCustomerAgreement() != null) {
            target.setCustomerAgreed(source.getCustomerAgreement());
        }
        if (source.getConfirmedAddress() != null) {
            target.setConfirmedAddress(source.getConfirmedAddress());
        }

        com.asrevo.cvhome.store.core.model.order.total.OrderTotal taxTotal = null;
        com.asrevo.cvhome.store.core.model.order.total.OrderTotal shippingTotal = null;


        if (source.getBilling() != null) {
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
                address.setCountry(source.getBilling().getCountry().getIsoCode());
            }
            if (source.getBilling().getZone() != null) {
                address.setZone(source.getBilling().getZone().getCode());
            }

            target.setBilling(address);
        }

        if (source.getOrderAttributes() != null && !source.getOrderAttributes().isEmpty()) {
            for (OrderAttribute attr : source.getOrderAttributes()) {
                com.asrevo.cvhome.store.core.model.order.OrderAttribute a = new com.asrevo.cvhome.store.core.model.order.OrderAttribute();
                a.setKey(attr.getKey());
                a.setValue(attr.getValue());
                target.getAttributes().add(a);
            }
        }

        if (source.getDelivery() != null) {
            ReadableDelivery address = new ReadableDelivery();
            address.setCity(source.getDelivery().getCity());
            address.setAddress(source.getDelivery().getAddress());
            address.setCompany(source.getDelivery().getCompany());
            address.setFirstName(source.getDelivery().getFirstName());
            address.setLastName(source.getDelivery().getLastName());
            address.setPostalCode(source.getDelivery().getPostalCode());
            address.setPhone(source.getDelivery().getTelephone());
            if (source.getDelivery().getCountry() != null) {
                address.setCountry(source.getDelivery().getCountry().getIsoCode());
            }
            if (source.getDelivery().getZone() != null) {
                address.setZone(source.getDelivery().getZone().getCode());
            }

            target.setDelivery(address);
        }

        List<com.asrevo.cvhome.store.core.model.order.total.OrderTotal> totals = new ArrayList<>();
        for (OrderTotal t : source.getOrderTotal()) {
            if (t.getOrderTotalType() == null) {
                continue;
            }
            if (t.getOrderTotalType().name().equals(OrderTotalType.TOTAL.name())) {
                com.asrevo.cvhome.store.core.model.order.total.OrderTotal totalTotal = createTotal(t);
                target.setTotal(totalTotal);
                totals.add(totalTotal);
            } else if (t.getOrderTotalType().name().equals(OrderTotalType.TAX.name())) {
                com.asrevo.cvhome.store.core.model.order.total.OrderTotal totalTotal = createTotal(t);
                if (taxTotal == null) {
                    taxTotal = totalTotal;
                } else {
                    BigDecimal v = taxTotal.getValue();
                    v = v.add(totalTotal.getValue());
                    taxTotal.setValue(v);
                }
                target.setTax(totalTotal);
                totals.add(totalTotal);
            } else if (t.getOrderTotalType().name().equals(OrderTotalType.SHIPPING.name())) {
                com.asrevo.cvhome.store.core.model.order.total.OrderTotal totalTotal = createTotal(t);
                if (shippingTotal == null) {
                    shippingTotal = totalTotal;
                } else {
                    BigDecimal v = shippingTotal.getValue();
                    v = v.add(totalTotal.getValue());
                    shippingTotal.setValue(v);
                }
                target.setShipping(totalTotal);
                totals.add(totalTotal);
            } else if (t.getOrderTotalType().name().equals(OrderTotalType.HANDLING.name())) {
                com.asrevo.cvhome.store.core.model.order.total.OrderTotal totalTotal = createTotal(t);
                if (shippingTotal == null) {
                    shippingTotal = totalTotal;
                } else {
                    BigDecimal v = shippingTotal.getValue();
                    v = v.add(totalTotal.getValue());
                    shippingTotal.setValue(v);
                }
                target.setShipping(totalTotal);
                totals.add(totalTotal);
            } else if (t.getOrderTotalType().name().equals(OrderTotalType.SUBTOTAL.name())) {
                com.asrevo.cvhome.store.core.model.order.total.OrderTotal subTotal = createTotal(t);
                totals.add(subTotal);

            } else {
                com.asrevo.cvhome.store.core.model.order.total.OrderTotal otherTotal = createTotal(t);
                totals.add(otherTotal);
            }
        }

        target.setTotals(totals);

        return target;
    }

    private com.asrevo.cvhome.store.core.model.order.total.OrderTotal createTotal(OrderTotal t) {
        com.asrevo.cvhome.store.core.model.order.total.OrderTotal totalTotal = new com.asrevo.cvhome.store.core.model.order.total.OrderTotal();
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
