package com.asrevo.cvhome.store.core.entity.shoppingcart;

import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import com.asrevo.cvhome.store.core.entity.catalog.product.price.FinalPrice;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditListener;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditSection;
import com.asrevo.cvhome.store.core.entity.common.audit.Auditable;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;


@Entity
@EntityListeners(value = AuditListener.class)
@Table(name = "SHOPPING_CART_ITEM")
@Getter
@Setter
public class ShoppingCartItem extends SalesManagerEntity<Long, ShoppingCartItem> implements Auditable, Serializable {


    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "SHP_CART_ITEM_ID", unique = true, nullable = false)
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT", pkColumnValue = "SHP_CRT_ITM_SEQ_NEXT_VAL")
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

    @Deprecated //Use sku
    @Column(name = "PRODUCT_ID", nullable = false)
    private Long productId;

    //SKU
    @Column(name = "SKU", nullable = true)
    private String sku;

    @JsonIgnore
    @Transient
    private boolean productVirtual;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "shoppingCartItem")
    private Set<ShoppingCartAttributeItem> attributes = new HashSet<ShoppingCartAttributeItem>();

    @Column(name = "PRODUCT_VARIANT", nullable = true)
    private Long variant;

    @JsonIgnore
    @Transient
    private BigDecimal itemPrice;//item final price including all rebates

    @JsonIgnore
    @Transient
    private BigDecimal subTotal;//item final price * quantity

    @JsonIgnore
    @Transient
    private FinalPrice finalPrice;//contains price details (raw prices)

    @JsonIgnore
    @Transient
    private Product product;

    @JsonIgnore
    @Transient
    private boolean obsolete = false;


    public ShoppingCartItem(ShoppingCart shoppingCart, Product product) {
        this(product);
        this.shoppingCart = shoppingCart;
    }

    public ShoppingCartItem(Product product) {
        this.product = product;
        this.productId = product.getId();
        this.setSku(product.getSku());
        this.quantity = 1;
        this.productVirtual = product.isProductVirtual();
    }
}
