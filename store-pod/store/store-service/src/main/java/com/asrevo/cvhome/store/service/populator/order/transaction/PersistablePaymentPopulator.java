package com.asrevo.cvhome.store.service.populator.order.transaction;

import com.asrevo.cvhome.store.controller.exception.ConversionException;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;
import com.asrevo.cvhome.store.core.entity.payments.TransactionType;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.order.transaction.PersistablePayment;
import com.asrevo.cvhome.store.core.model.payments.Payment;
import com.asrevo.cvhome.store.core.services.catalog.pricing.PricingService;
import com.asrevo.cvhome.store.utils.AbstractDataPopulator;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

public class PersistablePaymentPopulator extends AbstractDataPopulator<PersistablePayment, Payment> {


    PricingService pricingService;


    @Override
    public Payment populate(PersistablePayment source, Payment target, MerchantStore store, Language language)
            throws ConversionException {

        Assert.notNull(source, "PersistablePayment cannot be null");
        Assert.notNull(pricingService, "pricingService must be set");
        if (target == null) {
            target = new Payment();
        }

        try {

            target.setAmount(pricingService.getAmount(source.getAmount()));
            target.setModuleName(source.getPaymentModule());
            target.setPaymentType(PaymentType.valueOf(source.getPaymentType()));
            target.setTransactionType(TransactionType.valueOf(source.getTransactionType()));

            Map<String, String> metadata = new HashMap<String, String>();
            metadata.put("paymentToken", source.getPaymentToken());
            target.setPaymentMetaData(metadata);

            return target;

        } catch (Exception e) {
            throw new ConversionException(e);
        }
    }

    @Override
    protected Payment createTarget() {
        // TODO Auto-generated method stub
        return null;
    }

    public PricingService getPricingService() {
        return pricingService;
    }

    public void setPricingService(PricingService pricingService) {
        this.pricingService = pricingService;
    }

}
