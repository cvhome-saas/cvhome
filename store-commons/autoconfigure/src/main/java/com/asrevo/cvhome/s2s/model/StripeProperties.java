package com.asrevo.cvhome.s2s.model;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("com.asrevo.cvhome.stripe")
public record StripeProperties(String key, String webhookSigningKey) {
}
