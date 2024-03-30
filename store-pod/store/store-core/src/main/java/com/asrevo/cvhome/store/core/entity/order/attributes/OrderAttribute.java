package com.asrevo.cvhome.store.core.entity.order.attributes;

import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import com.asrevo.cvhome.store.core.entity.order.Order;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Entity used for storing various attributes related to an Order
 *
 * @author c.samson
 */
@Entity
@Table(name = "ORDER_ATTRIBUTE")
@Getter
@Setter
public class OrderAttribute extends SalesManagerEntity<Long, OrderAttribute> {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ORDER_ATTRIBUTE_ID", unique = true, nullable = false)
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT", pkColumnValue = "ORDER_ATTR_ID_NEXT_VALUE")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @Column(name = "IDENTIFIER", nullable = false)
    private String key;

    @Column(name = "VALUE", nullable = false)
    private String value;

    @ManyToOne(targetEntity = Order.class)
    @JoinColumn(name = "ORDER_ID", nullable = false)
    private Order order;

}
