package com.asrevo.cvhome.catalog.model.type;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotEmpty;

import com.asrevo.cvhome.commons.domain.Entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PersistableProductType extends Entity {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotEmpty
    private String code;

    private boolean allowAddToCart;

    private boolean visible;

    private List<ProductTypeDescription> descriptions = new ArrayList<>();
}
