package com.asrevo.cvhome.store.service.mapper.order;

import com.asrevo.cvhome.store.controller.exception.ConversionRuntimeException;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.order.OrderTotal;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.order.total.ReadableOrderTotal;
import com.asrevo.cvhome.store.core.services.catalog.pricing.PricingService;
import com.asrevo.cvhome.store.service.mapper.Mapper;
import com.asrevo.cvhome.store.utils.LabelUtils;
import com.asrevo.cvhome.store.utils.LocaleUtils;
import java.util.Locale;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Component;

@Component
public class ReadableOrderTotalMapper implements Mapper<OrderTotal, ReadableOrderTotal> {

    private final PricingService pricingService;

    private final LabelUtils messages;

    public ReadableOrderTotalMapper(PricingService pricingService, LabelUtils messages) {
        this.pricingService = pricingService;
        this.messages = messages;
    }

    @Override
    public ReadableOrderTotal convert(OrderTotal source, MerchantStore store, Language language) {
        ReadableOrderTotal destination = new ReadableOrderTotal();
        return this.merge(source, destination, store, language);
    }

    @Override
    public ReadableOrderTotal merge(
            OrderTotal source, ReadableOrderTotal target, MerchantStore store, Language language) {

        Validate.notNull(source, "OrderTotal must not be null");
        Validate.notNull(target, "ReadableTotal must not be null");
        Validate.notNull(store, "MerchantStore must not be null");
        Validate.notNull(language, "Language must not be null");

        Locale locale = LocaleUtils.getLocale(language);

        try {

            target.setCode(source.getOrderTotalCode());
            target.setId(source.getId());
            target.setModule(source.getModule());
            target.setOrder(source.getSortOrder());

            target.setTitle(
                    messages.getMessage(
                            source.getOrderTotalCode(), locale, source.getOrderTotalCode()));
            target.setText(source.getText());

            target.setValue(source.getValue());
            target.setTotal(pricingService.getDisplayAmount(source.getValue(), store));

            if (!StringUtils.isBlank(source.getOrderTotalCode())) {
                if (Constants.OT_DISCOUNT_TITLE.equals(source.getOrderTotalCode())) {
                    target.setDiscounted(true);
                }
            }

        } catch (Exception e) {
            throw new ConversionRuntimeException(e);
        }

        return target;
    }
}
