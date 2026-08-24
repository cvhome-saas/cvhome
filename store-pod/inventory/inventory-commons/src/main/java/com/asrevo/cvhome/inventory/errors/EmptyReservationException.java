package com.asrevo.cvhome.inventory.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ValidationException;

/**
 * A reservation was requested with no lines on it.
 *
 * <p>
 * Separate from {@link InsufficientInventoryException} because the two have different audiences: this one says the
 * calling code built a bad request, the other says the shopper cannot have what they asked for.
 * </p>
 */
public class EmptyReservationException extends ValidationException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected EmptyReservationException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static EmptyReservationException of(String ref) {
        return new ErrorBuilder<>(InventoryErrors.RESERVATION_EMPTY, EmptyReservationException::new)
                .detail("Reservation %s carries no entries.", ref)
                .param("ref", ref)
                .build();
    }

}
