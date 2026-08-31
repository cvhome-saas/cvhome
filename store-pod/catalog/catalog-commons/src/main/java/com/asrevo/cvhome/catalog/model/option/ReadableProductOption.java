package com.asrevo.cvhome.catalog.model.option;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import com.asrevo.cvhome.commons.domain.Entity;

import lombok.Getter;
import lombok.Setter;

/**
 * A store option with its values. {@code name} is the requested language's copy; {@code descriptions} carries every
 * language for the console editor.
 */
@Getter
@Setter
public class ReadableProductOption extends Entity {

    @Serial
    private static final long serialVersionUID = 1L;

    private String code;

    private String name;

    private Integer sortOrder;

    private List<ProductOptionDescription> descriptions = new ArrayList<>();

    private List<ReadableProductOptionValue> values = new ArrayList<>();
}
