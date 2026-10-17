package com.asrevo.cvhome.payment.api.v1.payment;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.payment.reads.PaymentTypeReads;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/public/payment-configuration")
@RequiredArgsConstructor
public class PublicPaymentConfigurationController {

    private final PaymentTypeReads reads;

    @GetMapping("/{storeId}/supported-payment-types")
    public PaymentType[] getSupportedPaymentTypes(@PathVariable("storeId") String storeId) {
        return reads.supported(new StoreMerchantId(storeId));
    }

}
