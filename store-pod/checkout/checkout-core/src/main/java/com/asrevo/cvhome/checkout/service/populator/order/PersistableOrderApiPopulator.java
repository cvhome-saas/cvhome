package com.asrevo.cvhome.checkout.service.populator.order;

import java.math.BigDecimal;
import java.time.Instant;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.asrevo.cvhome.checkout.entity.order.Order;
import com.asrevo.cvhome.checkout.entity.order.OrderChannel;
import com.asrevo.cvhome.checkout.entity.order.orderstatus.OrderStatusHistory;
import com.asrevo.cvhome.checkout.model.order.v1.PersistableOrder;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.merchant.model.merchant.ReadableMerchantStore;
import com.asrevo.cvhome.store.core.entity.common.InventoryStatus;
import com.asrevo.cvhome.store.core.entity.common.PaymentStatus;
import com.asrevo.cvhome.store.core.entity.order.orderstatus.OrderStatus;
import com.asrevo.cvhome.store.core.exception.ConversionException;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.store.core.populator.AbstractDataPopulator;
import com.asrevo.cvhome.store.utils.LocaleUtils;

@Component
public class PersistableOrderApiPopulator extends AbstractDataPopulator<PersistableOrder, StoreMerchantId, Order> {

    private final ExternalMerchantStoreService externalMerchantStoreService;

    public PersistableOrderApiPopulator(ExternalMerchantStoreService externalMerchantStoreService) {
        this.externalMerchantStoreService = externalMerchantStoreService;
    }

    @Override
    public Order populate(PersistableOrder source, Order target, StoreMerchantId store, LanguageCode language)
            throws ConversionException {

        try {

            if (target == null) {
                target = new Order();
            }

            ReadableMerchantStore baseStore = externalMerchantStoreService.getStore(store);
            target.setLocale(LocaleUtils.getLocale(baseStore.getDefaultLanguage()));

            target.setDatePurchased(Instant.now());
            target.setCurrency(baseStore.getCurrency());
            target.setCurrencyValue(new BigDecimal(0));
            target.setStoreMerchantId(store);
            target.setChannel(OrderChannel.API);
            // need this
            target.setPaymentType(source.getPaymentType());
            target.setStatus(OrderStatus.CREATED);
            target.setInventoryStatus(InventoryStatus.RESERVED);
            target.setPaymentStatus(PaymentStatus.PENDING);

            target.setCustomerAgreement(source.isCustomerAgreement());
            target.setConfirmedAddress(true);

            if (!StringUtils.isBlank(source.getComments())) {
                OrderStatusHistory statusHistory = new OrderStatusHistory();
                statusHistory.setStatus(null);
                statusHistory.setOrder(target);
                statusHistory.setComments(source.getComments());
                target.getOrderHistory().add(statusHistory);
            }

            return target;

        } catch (Exception e) {
            throw new ConversionException(e);
        }
    }

    @Override
    protected Order createTarget() {

        return null;
    }

}
