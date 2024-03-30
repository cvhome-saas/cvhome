package com.asrevo.cvhome.store.core.entity.catalog.product.price;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.common.description.Description;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "PRODUCT_PRICE_DESCRIPTION",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {
                        "PRODUCT_PRICE_ID",
                        "LANGUAGE_ID"
                })
        }
)

@TableGenerator(name = "description_gen", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT", pkColumnValue = "product_price_description_seq", allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE, initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
@Getter
@Setter
public class ProductPriceDescription extends Description {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    @JsonIgnore
    @ManyToOne(targetEntity = ProductPrice.class)
    @JoinColumn(name = "PRODUCT_PRICE_ID", nullable = false)
    private ProductPrice productPrice;


    @Column(name = "PRICE_APPENDER")
    private String priceAppender;

    public ProductPriceDescription() {
    }




}
