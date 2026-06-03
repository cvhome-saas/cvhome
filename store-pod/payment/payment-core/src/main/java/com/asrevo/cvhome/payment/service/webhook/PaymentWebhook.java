package com.asrevo.cvhome.payment.service.webhook;

import java.util.Map;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.payment.entity.payment.PaymentConfiguration;

public interface PaymentWebhook {
    void handleFailedWebhook(Long orderId, StoreMerchantId store, String payload, Map<String, Object> headers, PaymentConfiguration configuration);

    void handleSuccessWebhook(Long orderId, StoreMerchantId storeMerchantId, String payload, Map<String, Object> headers, PaymentConfiguration config);
}
