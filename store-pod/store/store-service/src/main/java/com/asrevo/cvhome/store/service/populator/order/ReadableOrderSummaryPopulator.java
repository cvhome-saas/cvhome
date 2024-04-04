package com.asrevo.cvhome.store.service.populator.order;

import com.asrevo.cvhome.store.controller.exception.ConversionException;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.order.OrderTotalSummary;
import com.asrevo.cvhome.store.core.model.order.ReadableOrderTotalSummary;
import com.asrevo.cvhome.store.core.model.order.total.ReadableOrderTotal;
import com.asrevo.cvhome.store.core.services.catalog.pricing.PricingService;
import com.asrevo.cvhome.store.utils.AbstractDataPopulator;
import com.asrevo.cvhome.store.utils.LabelUtils;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

@Setter
@Getter
public class ReadableOrderSummaryPopulator extends AbstractDataPopulator<OrderTotalSummary, ReadableOrderTotalSummary> {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(ReadableOrderSummaryPopulator.class);

    private PricingService pricingService;

    private LabelUtils messages;


    @Override
    public ReadableOrderTotalSummary populate(OrderTotalSummary source, ReadableOrderTotalSummary target,
                                              MerchantStore store, Language language) throws ConversionException {

        Assert.notNull(pricingService, "PricingService must be set");
        Assert.notNull(messages, "LabelUtils must be set");

        if (target == null) {
            target = new ReadableOrderTotalSummary();
        }

        try {

            if (source.getSubTotal() != null) {
                target.setSubTotal(pricingService.getDisplayAmount(source.getSubTotal(), store));
            }
            if (source.getTaxTotal() != null) {
                target.setTaxTotal(pricingService.getDisplayAmount(source.getTaxTotal(), store));
            }
            if (source.getTotal() != null) {
                target.setTotal(pricingService.getDisplayAmount(source.getTotal(), store));
            }

            if (!CollectionUtils.isEmpty(source.getTotals())) {
                ReadableOrderTotalPopulator orderTotalPopulator = new ReadableOrderTotalPopulator();
                orderTotalPopulator.setMessages(messages);
                orderTotalPopulator.setPricingService(pricingService);
                for (com.asrevo.cvhome.store.core.entity.order.OrderTotal orderTotal : source.getTotals()) {
                    ReadableOrderTotal t = new ReadableOrderTotal();
                    orderTotalPopulator.populate(orderTotal, t, store, language);
                    target.getTotals().add(t);
                }
            }


        } catch (Exception e) {
            LOGGER.error("Error during amount formatting " + e.getMessage());
            throw new ConversionException(e);
        }

        return target;

    }

    @Override
    protected ReadableOrderTotalSummary createTarget() {
        // TODO Auto-generated method stub
        return null;
    }


}
