package com.asrevo.cvhome.catalog.model.option;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import com.asrevo.cvhome.commons.domain.Entity;

import lombok.Getter;
import lombok.Setter;

/**
 * One value of a store option, with its name resolved for the requested language ({@code name}) and, for the
 * console, every language's copy ({@code descriptions}).
 */
@Getter
@Setter
public class ReadableProductOptionValue extends Entity {

    @Serial
    private static final long serialVersionUID = 1L;

    private String code;

    private String name;

    private Integer sortOrder;

    private List<ProductOptionDescription> descriptions = new ArrayList<>();
}
