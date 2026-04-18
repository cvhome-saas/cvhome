package com.asrevo.cvhome.catalog.entity.product.description;

import java.io.Serial;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import jakarta.persistence.UniqueConstraint;

import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.common.description.Description;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "PRODUCT_DESCRIPTION",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"PRODUCT_ID", "LANGUAGE_CODE"})},
        indexes = {@Index(name = "PRODUCT_DESCRIPTION_SEF_URL", columnList = "SEF_URL")})
@TableGenerator(name = "description_gen", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
        valueColumnName = "SEQ_COUNT", pkColumnValue = "PRODUCT_DESCRIPTION_SEQ_NEXT_VAL",
        allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
        initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
@Getter
@Setter
public class ProductDescription extends Description {

    @Serial
    private static final long serialVersionUID = 1L;

    @JsonIgnore
    @ManyToOne(targetEntity = Product.class)
    @JoinColumn(name = "PRODUCT_ID", nullable = false)
    private Product product;

    @Column(name = "PRODUCT_HIGHLIGHT")
    private String productHighlight;

    @Column(name = "DOWNLOAD_LNK")
    private String productExternalDl;

    @Column(name = "SEF_URL")
    private String seUrl;

    @Column(name = "META_TITLE")
    private String metatagTitle;

    @Column(name = "META_KEYWORDS")
    private String metatagKeywords;

    @Column(name = "META_DESCRIPTION")
    private String metatagDescription;

    public ProductDescription() {
    }

}
