package com.asrevo.cvhome.store.core.entity.shoppingcart;

import com.asrevo.cvhome.store.core.entity.catalog.product.attribute.ProductAttribute;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditListener;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditSection;
import com.asrevo.cvhome.store.core.entity.common.audit.Auditable;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;


@Entity
@EntityListeners(value = AuditListener.class)
@Table(name = "SHOPPING_CART_ATTR_ITEM")
@Getter
@Setter
public class ShoppingCartAttributeItem extends SalesManagerEntity<Long, ShoppingCartAttributeItem> implements Auditable {


    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "SHP_CART_ATTR_ITEM_ID", unique = true, nullable = false)
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT", pkColumnValue = "SHP_CRT_ATTR_ITM_SEQ_NEXT_VAL")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @Embedded
    private AuditSection auditSection = new AuditSection();


    @Column(name = "PRODUCT_ATTR_ID", nullable = false)
    private Long productAttributeId;

    @JsonIgnore
    @Transient
    private ProductAttribute productAttribute;


    @JsonIgnore
    @ManyToOne(targetEntity = ShoppingCartItem.class)
    @JoinColumn(name = "SHP_CART_ITEM_ID", nullable = false)
    private ShoppingCartItem shoppingCartItem;

    public ShoppingCartAttributeItem(ShoppingCartItem shoppingCartItem, ProductAttribute productAttribute) {
        this.shoppingCartItem = shoppingCartItem;
        this.productAttribute = productAttribute;
        this.productAttributeId = productAttribute.getId();
    }

    public ShoppingCartAttributeItem(ShoppingCartItem shoppingCartItem, Long productAttributeId) {
        this.shoppingCartItem = shoppingCartItem;
        this.productAttributeId = productAttributeId;
    }

    public ShoppingCartAttributeItem() {
    }
}
