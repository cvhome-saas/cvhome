package com.asrevo.cvhome.catalog.model.product;

import java.io.Serial;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One resolved (option, value) pair of a variant, with codes and the requested language's labels — what a cart
 * line, an order line or the console matrix renders as "Color: Red".
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReadableVariantOptionValue implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long optionId;

    private String optionCode;

    private String optionName;

    private Long valueId;

    private String valueCode;

    private String valueName;

    private Integer sortOrder;
}
