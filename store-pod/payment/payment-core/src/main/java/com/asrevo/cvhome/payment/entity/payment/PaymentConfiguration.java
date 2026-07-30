package com.asrevo.cvhome.payment.entity.payment;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "PAYMENT_CONFIGURATION")
@Getter
@Setter
public class PaymentConfiguration implements PaymentSecret {

    @EmbeddedId
    private PaymentConfigurationId id;

    @Column(name = "API_KEY")
    private String apiKey;

    @Column(name = "SECRET_KEY")
    private String secretKey;

    @Column(name = "WEBHOOK_SECRET")
    private String webhookSecret;

    @Column(name = "ENABLED")
    private boolean enabled;
}
