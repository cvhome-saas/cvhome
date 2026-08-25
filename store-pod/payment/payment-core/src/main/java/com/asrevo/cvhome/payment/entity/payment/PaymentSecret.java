package com.asrevo.cvhome.payment.entity.payment;

public interface PaymentSecret {
    String getApiKey();

    String getSecretKey();

    String getWebhookSecret();
}
