package com.asrevo.cvhome.payment.config;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.asrevo.cvhome.payment.model.payment.DefaultPaymentConfig;

@Configuration
public class PaymentConfig {
    @Bean
    public DefaultPaymentConfig defaultPaymentConfig() {
        return new DefaultPaymentConfig(Duration.ofMinutes(15));
    }

}
