package com.asrevo.cvhome.catalog.model.option;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

import com.asrevo.cvhome.commons.domain.Entity;

import lombok.Getter;
import lombok.Setter;

/**
 * A store option with its values, written as one document — the values list replaces the option's whole value set
 * (ids keep existing rows).
 */
@Getter
@Setter
public class PersistableProductOption extends Entity {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotEmpty
    @Pattern(regexp = "^[a-zA-Z0-9_-]*$")
    private String code;

    private Integer sortOrder;

    private List<ProductOptionDescription> descriptions = new ArrayList<>();

    @Valid
    private List<PersistableProductOptionValue> values = new ArrayList<>();
}
