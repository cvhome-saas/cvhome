package com.asrevo.cvhome.catalog.entity;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One searchable document per product per language.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchIndexId implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long productId;

    private String languageCode;

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ProductSearchIndexId that)) {
            return false;
        }
        return Objects.equals(productId, that.productId) && Objects.equals(languageCode, that.languageCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, languageCode);
    }
}
