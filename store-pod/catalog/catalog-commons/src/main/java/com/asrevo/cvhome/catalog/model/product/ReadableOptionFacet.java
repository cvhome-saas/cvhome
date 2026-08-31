package com.asrevo.cvhome.catalog.model.product;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * One option's slice of the filter rail: the option's identity and, per value that appears in the current
 * results, how many products selecting it would leave.
 */
@Getter
@Setter
public class ReadableOptionFacet implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long optionId;

    private String code;

    private String name;

    private Integer sortOrder;

    private List<ReadableFacetBucket> values = new ArrayList<>();
}
