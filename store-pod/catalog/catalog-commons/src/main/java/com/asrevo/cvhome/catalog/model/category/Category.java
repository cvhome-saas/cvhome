package com.asrevo.cvhome.catalog.model.category;

import java.io.Serial;
import java.io.Serializable;

import com.asrevo.cvhome.commons.domain.Entity;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Category extends Entity implements Serializable {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private String code;

    private CategoryDescription description;

}
