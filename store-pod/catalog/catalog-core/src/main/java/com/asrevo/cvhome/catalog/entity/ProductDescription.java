package com.asrevo.cvhome.catalog.entity;

import java.io.Serial;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import jakarta.persistence.UniqueConstraint;

import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.common.description.BaseDescription;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "PRODUCT_DESCRIPTION",
        uniqueConstraints = @UniqueConstraint(columnNames = {"PRODUCT_ID", "LANGUAGE_CODE"}),
        indexes = @Index(name = "PRODUCT_DESCRIPTION_SEF_URL", columnList = "SEF_URL"))
@TableGenerator(name = "description_gen", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
        valueColumnName = "SEQ_COUNT", pkColumnValue = "PRODUCT_DESCRIPTION_SEQ_NEXT_VAL",
        allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
        initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
@Getter
@Setter
public class ProductDescription extends BaseDescription {

    @Serial
    private static final long serialVersionUID = 1L;

    @ManyToOne
    @JoinColumn(name = "PRODUCT_ID", nullable = false)
    private Product product;

    @Column(name = "PRODUCT_HIGHLIGHT")
    private String highlight;

    /**
     * The storefront slug.
     */
    @Column(name = "SEF_URL")
    private String seUrl;

    @Column(name = "META_TITLE")
    private String metaTitle;

    @Column(name = "META_KEYWORDS")
    private String metaKeywords;

    @Column(name = "META_DESCRIPTION")
    private String metaDescription;

    public ProductDescription() {
    }

    public ProductDescription(Product product) {
        this.product = product;
    }
}
