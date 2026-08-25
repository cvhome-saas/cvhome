package com.asrevo.cvhome.catalog.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ResourceNotFoundException;

/**
 * No product attribute with that id exists on the product in this store.
 *
 * <p>
 * One code for what used to be three separate messages — the attribute is missing, it belongs to a different product,
 * or that product belongs to a different store. To a caller they are the same question, and answering them
 * differently would confirm which of the three ids was the real one.
 * </p>
 */
public class ProductAttributeNotFoundException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ProductAttributeNotFoundException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static ProductAttributeNotFoundException of(Object attributeId, Object store) {
        return new ErrorBuilder<>(CatalogErrors.PRODUCT_ATTRIBUTE_NOT_FOUND, ProductAttributeNotFoundException::new)
                .detail("No product attribute %s in store %s.", attributeId, store)
                .param("attributeId", attributeId)
                .param("store", store)
                .build();
    }

}
