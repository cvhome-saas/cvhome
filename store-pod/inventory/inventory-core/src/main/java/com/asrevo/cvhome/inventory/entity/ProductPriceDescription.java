package com.asrevo.cvhome.inventory.entity;

import java.io.Serial;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import jakarta.persistence.UniqueConstraint;

import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.common.description.BaseDescription;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "PRODUCT_PRICE_DESCRIPTION",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"PRODUCT_PRICE_ID", "LANGUAGE_CODE"})})
@TableGenerator(name = "description_gen", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
        valueColumnName = "SEQ_COUNT", pkColumnValue = "PRODUCT_PRICE_DESCRIPTION_SEQ_NEXT_VAL",
        allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
        initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
@Getter
@Setter
public class ProductPriceDescription extends BaseDescription {

    @Serial
    private static final long serialVersionUID = 1L;

    @JsonIgnore
    @ManyToOne(targetEntity = ProductPrice.class)
    @JoinColumn(name = "PRODUCT_PRICE_ID", nullable = false)
    private ProductPrice productPrice;

    @Column(name = "PRICE_APPENDER")
    private String priceAppender;

}
