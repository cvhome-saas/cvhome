package com.asrevo.cvhome.content.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.OperationNotAllowedException;

public class BannerArtworkRequiredException extends OperationNotAllowedException {
    @Serial
    private static final long serialVersionUID = 1L;

    protected BannerArtworkRequiredException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static BannerArtworkRequiredException create() {
        return new ErrorBuilder<>(ContentErrors.BANNER_ARTWORK_REQUIRED, BannerArtworkRequiredException::new)
                .detail("Default-language banner artwork requires media and alternative text.")
                .build();
    }
}
