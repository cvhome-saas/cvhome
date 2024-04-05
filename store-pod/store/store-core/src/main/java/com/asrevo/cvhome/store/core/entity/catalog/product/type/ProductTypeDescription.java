package com.asrevo.cvhome.store.core.entity.catalog.product.type;

import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.common.description.Description;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

@Entity
@Table(name = "PRODUCT_TYPE_DESCRIPTION",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"PRODUCT_TYPE_ID", "LANGUAGE_ID"})})

@TableGenerator(name = "description_gen", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT", pkColumnValue = "product_type_description_seq", allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE, initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
@Getter
@Setter
public class ProductTypeDescription extends Description {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    @ManyToOne(targetEntity = ProductType.class)
    @JoinColumn(name = "PRODUCT_TYPE_ID", nullable = false)
    private ProductType productType;

}
