package com.asrevo.cvhome.catalog.model.group;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotEmpty;

import com.asrevo.cvhome.commons.domain.Entity;

import lombok.Getter;
import lombok.Setter;

/**
 * A product group as written. The save is an upsert on {@code code}; the body replaces descriptions and members.
 */
@Getter
@Setter
public class PersistableProductGroup extends Entity {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotEmpty
    private String code;

    private boolean active = true;

    private Long parentProductId;

    private List<Long> productIds = new ArrayList<>();

    private List<ProductGroupDescription> descriptions = new ArrayList<>();
}
