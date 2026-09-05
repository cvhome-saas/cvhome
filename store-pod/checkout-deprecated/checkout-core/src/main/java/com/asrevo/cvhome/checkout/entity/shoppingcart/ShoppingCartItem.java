package com.asrevo.cvhome.checkout.entity.shoppingcart;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import jakarta.persistence.Transient;

import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditListener;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditSection;
import com.asrevo.cvhome.store.core.entity.common.audit.Auditable;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;

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
            valueColumnName = "SEQ_COUNT", pkColumnValue = "SHOPPING_CART_ITEM_SEQ_NEXT_VAL",
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

    /**
     * The whole business key of a cart line — always a variant sku since the uniform variant model (the
     * inventory and catalog sides cannot tell a "simple" sku apart, and neither can we).
     */
    @Column(name = "SKU")
    private String sku;

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
