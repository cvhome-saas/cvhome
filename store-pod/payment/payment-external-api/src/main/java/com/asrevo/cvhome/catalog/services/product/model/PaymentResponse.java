package com.asrevo.cvhome.catalog.services.product.model;


public record PaymentResponse(
        PaymentStatus status,
        String redirectUrl,
        boolean isRedirect) { // Added boolean flag
}