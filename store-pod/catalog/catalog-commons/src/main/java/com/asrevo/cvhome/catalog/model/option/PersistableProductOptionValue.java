package com.asrevo.cvhome.catalog.model.option;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

import com.asrevo.cvhome.commons.domain.Entity;

import lombok.Getter;
import lombok.Setter;

/**
 * One value inside a {@link PersistableProductOption} write. Carrying the id keeps the existing row (and the variant
 * rows that reference it); a value absent from the write is removed.
 */
@Getter
@Setter
public class PersistableProductOptionValue extends Entity {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotEmpty
    @Pattern(regexp = "^[a-zA-Z0-9_-]*$")
    private String code;

    private Integer sortOrder;

    private List<ProductOptionDescription> descriptions = new ArrayList<>();
}
