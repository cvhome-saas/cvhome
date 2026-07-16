package com.asrevo.cvhome.payment.models;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReadablePaymentConfiguration {
    private Long id;
    private StoreMerchantId storeMerchantId;
    private PaymentType paymentType;
    private String apiKey;
    private String secretKey;
    private String webhookSecret;
    private boolean enabled;
}
