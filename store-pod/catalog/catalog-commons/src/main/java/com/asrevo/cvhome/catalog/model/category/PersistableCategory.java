package com.asrevo.cvhome.catalog.model.category;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotEmpty;

import com.asrevo.cvhome.commons.domain.Entity;

import lombok.Getter;
import lombok.Setter;

/**
 * A category as written. The parent is a reference (id or code); re-parenting an existing category goes through
 * the move endpoint, not through this body.
 */
@Getter
@Setter
public class PersistableCategory extends Entity {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotEmpty
    private String code;

    private int sortOrder;

    private boolean visible;

    private boolean featured;

    private CategoryReference parent;

    private List<CategoryDescription> descriptions = new ArrayList<>();
}
