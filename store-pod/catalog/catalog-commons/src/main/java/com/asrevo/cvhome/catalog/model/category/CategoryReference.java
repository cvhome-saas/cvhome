package com.asrevo.cvhome.catalog.model.category;

import java.io.Serial;
import java.io.Serializable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The shallow way one payload points at a category: by id, by code, or both. Used for a category's parent and for
 * a product's categories.
 */
@Getter
@Setter
@NoArgsConstructor
public class CategoryReference implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String code;

    public CategoryReference(Long id, String code) {
        this.id = id;
        this.code = code;
    }
}
