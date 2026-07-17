package com.asrevo.cvhome.checkout.service.mapper.order;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.asrevo.cvhome.checkout.entity.order.OrderTotal;
import com.asrevo.cvhome.checkout.model.order.total.ReadableOrderTotal;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.store.controller.exception.ConversionRuntimeException;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.mapper.Mapper;
import com.asrevo.cvhome.store.utils.PriceUtils;

@Component
public class ReadableOrderTotalMapper implements Mapper<OrderTotal, ReadableOrderTotal> {

    private final ExternalMerchantStoreService externalMerchantStoreService;

    public ReadableOrderTotalMapper(ExternalMerchantStoreService externalMerchantStoreService) {
        this.externalMerchantStoreService = externalMerchantStoreService;
    }

    @Override
    public ReadableOrderTotal convert(OrderTotal source, StoreMerchantId store, LanguageCode language) {
        ReadableOrderTotal destination = new ReadableOrderTotal();
        return this.merge(source, destination, store, language);
    }

    @Override
    public ReadableOrderTotal merge(OrderTotal source, ReadableOrderTotal target, StoreMerchantId store,
                                    LanguageCode language) {

        try {

            target.setCode(source.getOrderTotalCode());
            target.setId(source.getId());
            target.setModule(source.getModule());
            target.setOrder(source.getSortOrder());

            target.setTitle(source.getOrderTotalCode());
            target.setText(source.getText());

            target.setValue(source.getValue());
            target.setTotal(PriceUtils.getStoreFormatedAmountWithCurrency(externalMerchantStoreService.getStore(store),
                    source.getValue()));

            if (!StringUtils.isBlank(source.getOrderTotalCode()) && Constants.OT_DISCOUNT_TITLE.equals(source.getOrderTotalCode())) {
                target.setDiscounted(true);
            }


        } catch (Exception e) {
            throw new ConversionRuntimeException(e);
        }

        return target;
    }

}
