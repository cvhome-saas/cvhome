package com.asrevo.cvhome.catalog.model.product;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

import com.asrevo.cvhome.commons.domain.Entity;

import lombok.Getter;
import lombok.Setter;

/**
 * One combination inside a {@link PersistableVariantSet}. Carrying the id keeps the existing row (its audit
 * trail and its sku history); {@code optionValueIds} must hold exactly one value of every option the set
 * declares.
 */
@Getter
@Setter
public class PersistableProductVariant extends Entity {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotEmpty
    @Pattern(regexp = "^[a-zA-Z0-9_-]*$")
    private String sku;

    private Integer sortOrder;

    private boolean defaultVariant;

    private List<Long> optionValueIds = new ArrayList<>();
}
