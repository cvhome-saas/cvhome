package com.asrevo.cvhome.catalog.entity;

import java.io.Serial;

import jakarta.persistence.Entity;
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
@Table(name = "PRODUCT_OPTION_VALUE_DESCRIPTION",
        uniqueConstraints = @UniqueConstraint(columnNames = {"PRODUCT_OPTION_VALUE_ID", "LANGUAGE_CODE"}))
@TableGenerator(name = "description_gen", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
        valueColumnName = "SEQ_COUNT", pkColumnValue = "PRODUCT_OPTION_VALUE_DESCRIPTION_SEQ_NEXT_VAL",
        allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
        initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
@Getter
@Setter
public class ProductOptionValueDescription extends BaseDescription {

    @Serial
    private static final long serialVersionUID = 1L;

    @ManyToOne
    @JoinColumn(name = "PRODUCT_OPTION_VALUE_ID", nullable = false)
    private ProductOptionValue optionValue;

    public ProductOptionValueDescription() {
    }

    public ProductOptionValueDescription(ProductOptionValue optionValue) {
        this.optionValue = optionValue;
    }
}
