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
 * One chosen value of one option on one variant. The primary key is {@code (variant, option)} — a variant can
 * hold at most one value per option, by construction.
 */
@Entity
@Table(name = "PRODUCT_VARIANT_OPTION_VALUE")
@Getter
@Setter
public class ProductVariantOptionValue implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private Key key = new Key();

    @MapsId("variantId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRODUCT_VARIANT_ID", nullable = false)
    private ProductVariant variant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRODUCT_OPTION_VALUE_ID", nullable = false)
    private ProductOptionValue optionValue;

    public ProductVariantOptionValue() {
    }

    public ProductVariantOptionValue(ProductVariant variant, ProductOption option, ProductOptionValue value) {
        this.variant = variant;
        this.optionValue = value;
        this.key = new Key(variant.getId(), option.getId());
    }

    @Embeddable
    @Getter
    @Setter
    public static class Key implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        @Column(name = "PRODUCT_VARIANT_ID")
        private Long variantId;

        @Column(name = "PRODUCT_OPTION_ID")
        private Long optionId;

        public Key() {
        }

        public Key(Long variantId, Long optionId) {
            this.variantId = variantId;
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
            return Objects.equals(variantId, key.variantId) && Objects.equals(optionId, key.optionId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(variantId, optionId);
        }
    }
}
