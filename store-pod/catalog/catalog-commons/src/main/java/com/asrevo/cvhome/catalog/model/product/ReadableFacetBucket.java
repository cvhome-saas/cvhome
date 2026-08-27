package com.asrevo.cvhome.catalog.model.product;

import java.io.Serial;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One option in a filter rail, with how many of the current results it would leave.
 *
 * <p>
 * The count is what stops a theme offering a filter that leads nowhere: buckets are computed over the same
 * predicate as the page, so a bucket that would empty the results simply is not in the list.
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReadableFacetBucket implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private long count;

    private boolean selected;
}
