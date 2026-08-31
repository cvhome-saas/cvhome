package com.asrevo.cvhome.catalog.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ValidationException;

/**
 * The write exceeds the guardrails (options per product, variants per product) that keep the matrix UI, the
 * facet queries and the availability calls bounded.
 */
public class VariantLimitExceededException extends ValidationException {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String GIVEN = "given";

    private static final String MAX = "max";

    protected VariantLimitExceededException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static VariantLimitExceededException options(int given, int max) {
        return new ErrorBuilder<>(CatalogErrors.PRODUCT_VARIANT_LIMIT_EXCEEDED, VariantLimitExceededException::new)
                .detail("A product may vary by at most %d options; %d were given.", max, given)
                .param(GIVEN, given)
                .param(MAX, max)
                .build();
    }

    public static VariantLimitExceededException variants(int given, int max) {
        return new ErrorBuilder<>(CatalogErrors.PRODUCT_VARIANT_LIMIT_EXCEEDED, VariantLimitExceededException::new)
                .detail("A product may hold at most %d variants; %d were given.", max, given)
                .param(GIVEN, given)
                .param(MAX, max)
                .build();
    }
}
