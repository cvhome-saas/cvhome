package com.asrevo.cvhome.catalog.entity;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

/**
 * One axis a product varies by — the product's ordered pick from the store's option vocabulary. Written together
 * with the variant set in one atomic replace, so axes and combinations can never disagree.
 */
@Entity
@Table(name = "PRODUCT_OPTION_ASSIGNMENT")
@Getter
@Setter
public class ProductOptionAssignment implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private Key key = new Key();

    @MapsId("productId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRODUCT_ID", nullable = false)
    private Product product;

    @MapsId("optionId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRODUCT_OPTION_ID", nullable = false)
    private ProductOption option;

    @Column(name = "SORT_ORDER", nullable = false)
    private int sortOrder;

    public ProductOptionAssignment() {
    }

    public ProductOptionAssignment(Product product, ProductOption option, int sortOrder) {
        this.product = product;
        this.option = option;
        this.sortOrder = sortOrder;
        this.key = new Key(product.getId(), option.getId());
    }

    @Embeddable
    @Getter
    @Setter
    public static class Key implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        @Column(name = "PRODUCT_ID")
        private Long productId;

        @Column(name = "PRODUCT_OPTION_ID")
        private Long optionId;

        public Key() {
        }

        public Key(Long productId, Long optionId) {
            this.productId = productId;
            this.optionId = optionId;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof Key key)) {
                return false;
            }
            return Objects.equals(productId, key.productId) && Objects.equals(optionId, key.optionId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(productId, optionId);
        }
    }
}
