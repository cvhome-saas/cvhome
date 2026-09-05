package com.asrevo.cvhome.checkout.model.signal;

import java.io.Serializable;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.asrevo.cvhome.store.core.entity.common.PaymentStatus;

/**
 * Payment telling checkout what became of an order's payment. {@code transactionRef} is payment's own reference for
 * the transaction; with the status it is the dedup key, so a redelivered event is a recorded no-op.
 */
public record PaymentSignal(@NotNull PaymentStatus status, @NotBlank String transactionRef) implements Serializable {
}
