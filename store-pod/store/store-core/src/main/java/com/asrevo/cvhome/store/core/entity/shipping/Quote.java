package com.asrevo.cvhome.store.core.entity.shipping;

import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.common.Delivery;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.Date;


/**
 * Shipping quote received from external shipping quote module or calculated internally
 *
 * @author c.samson
 */

@Entity
@Table(name = "SHIPPING_QUOTE")
@Getter
@Setter
public class Quote extends SalesManagerEntity<Long, Quote> {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "SHIPPING_QUOTE_ID", unique = true, nullable = false)
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT", pkColumnValue = "SHIP_QUOTE_ID_NEXT_VALUE", allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE, initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    Long id;

    @Column(name = "ORDER_ID")
    private Long orderId;

    @Column(name = "CUSTOMER_ID")
    private Long customerId;

    @Column(name = "CART_ID")
    private Long cartId;

    @Column(name = "MODULE", nullable = false)
    private String module;

    @Column(name = "OPTION_NAME")
    private String optionName = null;

    @Column(name = "OPTION_CODE")
    private String optionCode = null;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "OPTION_DELIVERY_DATE")
    private Date optionDeliveryDate = null;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "OPTION_SHIPPING_DATE")
    private Date optionShippingDate = null;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "QUOTE_DATE")
    private Date quoteDate;

    @Column(name = "SHIPPING_NUMBER_DAYS")
    private Integer estimatedNumberOfDays;

    @Column(name = "QUOTE_PRICE")
    private BigDecimal price = null;

    @Column(name = "QUOTE_HANDLING")
    private BigDecimal handling = null;

    @Column(name = "FREE_SHIPPING")
    private boolean freeShipping;

    @Column(name = "IP_ADDRESS")
    private String ipAddress;

    @Embedded
    private Delivery delivery = null;

}
