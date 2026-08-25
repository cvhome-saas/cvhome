package com.asrevo.cvhome.catalog.model.category;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import com.asrevo.cvhome.commons.domain.Entity;

import lombok.Getter;
import lombok.Setter;

/**
 * A category as read. {@code description} is the copy in the language asked for; {@code descriptions} carries every
 * language on the private endpoints. {@code children} is filled by the hierarchy reads and by the single read.
 */
@Getter
@Setter
public class ReadableCategory extends Entity {

    @Serial
    private static final long serialVersionUID = 1L;

    private String code;

    private int sortOrder;

    private boolean visible;

    private boolean featured;

    /**
     * Materialised path of ids, e.g. {@code /1/7/}.
     */
    private String lineage;

    private int depth;

    private String store;

    private CategoryReference parent;

    private CategoryDescription description;

    private List<CategoryDescription> descriptions = new ArrayList<>();

    private List<ReadableCategory> children = new ArrayList<>();
}
