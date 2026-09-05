package com.asrevo.cvhome.checkout.entity;

import java.io.Serial;
import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;

import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * One line of the totals block. Today only SUBTOTAL and TOTAL exist; SHIPPING and TAX are reserved codes.
 */
@Entity
@Table(name = "SALES_ORDER_TOTAL")
@Getter
@Setter
public class OrderTotal extends SalesManagerEntity<Long, OrderTotal> {

    public static final String SUBTOTAL = "SUBTOTAL";

    public static final String TOTAL = "TOTAL";

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "TOTAL_ID")
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT", pkColumnValue = "SALES_ORDER_TOTAL_SEQ_NEXT_VAL",
            allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
            initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ORDER_ID", nullable = false)
    private Order order;

    @Column(name = "CODE", nullable = false, length = 20)
    private String code;

    @Column(name = "MODULE", nullable = false, length = 60)
    private String module;

    @Column(name = "TITLE")
    private String title;

    @Column(name = "VALUE", nullable = false, precision = 19, scale = 4)
    private BigDecimal value;

    @Column(name = "SORT_ORDER", nullable = false)
    private int sortOrder;

    public OrderTotal() {
    }

    public OrderTotal(Order order, String code, String module, BigDecimal value, int sortOrder) {
        this.order = order;
        this.code = code;
        this.module = module;
        this.value = value;
        this.sortOrder = sortOrder;
    }
}
