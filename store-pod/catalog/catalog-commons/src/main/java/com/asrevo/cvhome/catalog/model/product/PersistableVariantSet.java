package com.asrevo.cvhome.catalog.model.product;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;

import lombok.Getter;
import lombok.Setter;

/**
 * The whole-set replace a product's variants are written as: the axes (option codes from the store vocabulary,
 * in display order) and the combinations, applied atomically so they can never desync. Empty options and
 * variants restore the single default variant; with axes declared, every variant must cover exactly those axes.
 */
@Getter
@Setter
public class PersistableVariantSet implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private List<String> options = new ArrayList<>();

    @Valid
    private List<PersistableProductVariant> variants = new ArrayList<>();
}
