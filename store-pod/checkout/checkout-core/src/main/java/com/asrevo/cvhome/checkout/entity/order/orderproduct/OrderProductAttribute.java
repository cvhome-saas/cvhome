package com.asrevo.cvhome.checkout.entity.order.orderproduct;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;

import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ORDER_PRODUCT_ATTRIBUTE")
@Getter
@Setter
public class OrderProductAttribute implements Serializable {

    @Serial
    private static final long serialVersionUID = 6037571119918073015L;

    @Id
    @Column(name = "ORDER_PRODUCT_ATTRIBUTE_ID", nullable = false, unique = true)
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT", pkColumnValue = "ORDER_PRODUCT_ATTRIBUTE_SEQ_NEXT_VAL",
            allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
            initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @Column(name = "PRODUCT_ATTRIBUTE_PRICE", nullable = false, precision = 15, scale = 4)
    private BigDecimal productAttributePrice;

    @Column(name = "PRODUCT_ATTRIBUTE_IS_FREE", nullable = false)
    private boolean productAttributeIsFree;

    @Column(name = "PRODUCT_ATTRIBUTE_WEIGHT", precision = 15, scale = 4)
    private java.math.BigDecimal productAttributeWeight;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "ORDER_PRODUCT_ID", nullable = false)
    private OrderProduct orderProduct;

    @Column(name = "PRODUCT_OPTION_ID", nullable = false)
    private Long productOptionId;

    @Column(name = "PRODUCT_OPTION_VALUE_ID", nullable = false)
    private Long productOptionValueId;

    @Column(name = "PRODUCT_ATTRIBUTE_NAME")
    private String productAttributeName;

    @Column(name = "PRODUCT_ATTRIBUTE_VAL_NAME")
    private String productAttributeValueName;

}
