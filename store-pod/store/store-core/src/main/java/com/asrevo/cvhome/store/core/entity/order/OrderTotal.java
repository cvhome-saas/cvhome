package com.asrevo.cvhome.store.core.entity.order;

import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;


/**
 * Order line items related to an order.
 *
 * @author casams1
 */

@Setter
@Getter
@Entity
@Table(name = "ORDER_TOTAL")
public class OrderTotal extends SalesManagerEntity<Long, OrderTotal> {
    private static final long serialVersionUID = -5885315557404081674L;

    @Id
    @Column(name = "ORDER_ACCOUNT_ID", unique = true, nullable = false)
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT", pkColumnValue = "ORDER_TOTAL_ID_NEXT_VALUE")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @Column(name = "CODE", nullable = false)
    private String orderTotalCode;//SHIPPING, TAX

    @Column(name = "TITLE", nullable = true)
    private String title;

    @Column(name = "TEXT", nullable = true, columnDefinition = "text")
    private String text;

    @Column(name = "VALUE", precision = 15, scale = 4, nullable = false)
    private BigDecimal value;

    @Column(name = "MODULE", length = 60, nullable = true)
    private String module;

    @Column(name = "ORDER_VALUE_TYPE")
    @Enumerated(value = EnumType.STRING)
    private OrderValueType orderValueType = OrderValueType.ONE_TIME;

    @Column(name = "ORDER_TOTAL_TYPE")
    @Enumerated(value = EnumType.STRING)
    private OrderTotalType orderTotalType = null;

    @Column(name = "SORT_ORDER", nullable = false)
    private int sortOrder;

    @JsonIgnore
    @ManyToOne(targetEntity = Order.class)
    @JoinColumn(name = "ORDER_ID", nullable = false)
    private Order order;

    public OrderTotal() {
    }


}