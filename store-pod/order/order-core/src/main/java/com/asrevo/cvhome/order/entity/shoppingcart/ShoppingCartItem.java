package com.asrevo.cvhome.order.entity.shoppingcart;

import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditListener;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditSection;
import com.asrevo.cvhome.store.core.entity.common.audit.Auditable;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Entity
@EntityListeners(value = AuditListener.class)
@Table(name = "SHOPPING_CART_ITEM")
@Getter
@Setter
public class ShoppingCartItem extends SalesManagerEntity<Long, ShoppingCartItem> implements Auditable, Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "SHP_CART_ITEM_ID", unique = true, nullable = false)
	@TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
			valueColumnName = "SEQ_COUNT", pkColumnValue = "SHP_CRT_ITM_SEQ_NEXT_VAL",
			allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
			initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
	private Long id;

	@JsonIgnore
	@ManyToOne(targetEntity = ShoppingCart.class)
	@JoinColumn(name = "SHP_CART_ID", nullable = false)
	private ShoppingCart shoppingCart;

	@Column(name = "QUANTITY")
	private Integer quantity = 1;

	@Embedded
	private AuditSection auditSection = new AuditSection();

	// SKU
	@Column(name = "SKU")
	private String sku;

	@Column(name = "PRODUCT_VARIANT")
	private Long variant;

	@JsonIgnore
	@Transient
	private BigDecimal itemPrice; // item final price including all rebates

	@JsonIgnore
	@Transient
	private BigDecimal subTotal; // item final price * quantity

	@JsonIgnore
	@Transient
	private boolean obsolete = false;

	public ShoppingCartItem(ShoppingCart shoppingCart, String sku) {
		this(sku);
		this.shoppingCart = shoppingCart;
	}

	public ShoppingCartItem(String sku) {
		this.sku = sku;
		this.quantity = 1;
	}

	public ShoppingCartItem() {
	}

}
