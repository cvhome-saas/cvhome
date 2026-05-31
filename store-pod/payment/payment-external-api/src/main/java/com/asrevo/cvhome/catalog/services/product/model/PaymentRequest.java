package com.asrevo.cvhome.catalog.services.product.model;

import java.math.BigDecimal;

import com.asrevo.cvhome.store.core.model.reference.CurrencyCode;

public record PaymentRequest(Long orderId, BigDecimal amount, CurrencyCode currency) {

}