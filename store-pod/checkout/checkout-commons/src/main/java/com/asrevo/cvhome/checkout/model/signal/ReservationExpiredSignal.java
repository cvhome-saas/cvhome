package com.asrevo.cvhome.checkout.model.signal;

import java.io.Serializable;

import jakarta.validation.constraints.NotBlank;

/**
 * Inventory telling checkout it released a reservation nobody committed in time.
 */
public record ReservationExpiredSignal(@NotBlank String reservationRef) implements Serializable {
}
