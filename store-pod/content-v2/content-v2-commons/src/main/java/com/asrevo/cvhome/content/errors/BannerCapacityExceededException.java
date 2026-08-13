package com.asrevo.cvhome.content.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.OperationNotAllowedException;

public class BannerCapacityExceededException extends OperationNotAllowedException {
    @Serial
    private static final long serialVersionUID = 1L;

    protected BannerCapacityExceededException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static BannerCapacityExceededException forPlacement(String placement, int capacity) {
        return new ErrorBuilder<>(ContentErrors.BANNER_CAPACITY_EXCEEDED, BannerCapacityExceededException::new)
                .detail("Banner placement capacity has been reached.")
                .param("placement", placement)
                .param("capacity", capacity)
                .build();
    }
}
