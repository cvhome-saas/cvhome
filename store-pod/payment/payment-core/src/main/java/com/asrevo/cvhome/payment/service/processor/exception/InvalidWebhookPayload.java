package com.asrevo.cvhome.payment.service.processor.exception;

public class InvalidWebhookPayload extends RuntimeException {

    public InvalidWebhookPayload(String message, Exception e) {
        super(message, e);
    }
}
