package com.asrevo.cvhome.checkout.entity.order;

import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.io.Serial;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

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

	@Serial
	private static final long serialVersionUID = -5885315557404081674L;

	@Id
	@Column(name = "ORDER_ACCOUNT_ID", unique = true, nullable = false)
	@TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
			valueColumnName = "SEQ_COUNT", pkColumnValue = "ORDER_TOTAL_SEQ_NEXT_VAL",
			allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
			initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
	private Long id;

	@Column(name = "CODE", nullable = false)
	private String orderTotalCode; // SHIPPING, TAX

	@Column(name = "TITLE")
	private String title;

	@Column(name = "TEXT", columnDefinition = "text")
	private String text;

	@Column(name = "VALUE", precision = 15, scale = 4, nullable = false)
	private BigDecimal value;

	@Column(name = "MODULE", length = 60)
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
