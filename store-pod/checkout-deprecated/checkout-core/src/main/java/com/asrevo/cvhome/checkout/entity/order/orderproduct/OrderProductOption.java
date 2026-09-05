package com.asrevo.cvhome.checkout.entity.order.orderproduct;

import java.io.Serial;
import java.io.Serializable;

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

/**
 * The option/value labels of the variant an order line sold ("Color: Red"), copied from the catalog at
 * placement time — an order must keep rendering its lines however the catalog is edited or emptied later.
 */
@Entity
@Table(name = "ORDER_PRODUCT_OPTION")
@Getter
@Setter
public class OrderProductOption implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ORDER_PRODUCT_OPTION_ID", nullable = false, unique = true)
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT", pkColumnValue = "ORDER_PRODUCT_OPTION_SEQ_NEXT_VAL",
            allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
            initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "ORDER_PRODUCT_ID", nullable = false)
    private OrderProduct orderProduct;

    @Column(name = "OPTION_CODE", nullable = false, length = 100)
    private String optionCode;

    @Column(name = "OPTION_NAME", nullable = false, length = 120)
    private String optionName;

    @Column(name = "VALUE_CODE", nullable = false, length = 100)
    private String valueCode;

    @Column(name = "VALUE_NAME", nullable = false, length = 120)
    private String valueName;

    @Column(name = "SORT_ORDER")
    private Integer sortOrder;
}
