/**
 *
 */
package com.asrevo.cvhome.store.core.entity.shoppingcart;

import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditListener;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditSection;
import com.asrevo.cvhome.store.core.entity.common.audit.Auditable;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>Shopping cart is responsible for storing and carrying
 * shopping cart information.Shopping Cart consists of {@link ShoppingCartItem}
 * which represents individual lines items associated with the shopping cart</p>
 *
 * @author Umesh Awasthi
 * version 2.0
 */
@Entity
@EntityListeners(value = AuditListener.class)
@Table(name = "SHOPPING_CART", indexes = {@Index(name = "SHP_CART_CODE_IDX", columnList = "SHP_CART_CODE"), @Index(name = "SHP_CART_CUSTOMER_IDX", columnList = "CUSTOMER_ID")})
@Getter
@Setter
public class ShoppingCart extends SalesManagerEntity<Long, ShoppingCart> implements Auditable {


    @Serial
    private static final long serialVersionUID = 1L;

    @Embedded
    private AuditSection auditSection = new AuditSection();

    @Id
    @Column(name = "SHP_CART_ID", unique = true, nullable = false)
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT", pkColumnValue = "SHP_CRT_SEQ_NEXT_VAL", allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE, initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    /**
     * Will be used to fetch shopping cart model from the controller
     * this is a unique code that should be attributed from the client (UI)
     */
    @Column(name = "SHP_CART_CODE", unique = true, nullable = false)
    private String shoppingCartCode;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "shoppingCart")
    private Set<ShoppingCartItem> lineItems = new HashSet<ShoppingCartItem>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MERCHANT_ID", nullable = false)
    private MerchantStore merchantStore;

    @Column(name = "CUSTOMER_ID", nullable = true)
    private Long customerId;

    @Column(name = "ORDER_ID", nullable = true)
    private Long orderId;

    @Column(name = "IP_ADDRESS")
    private String ipAddress;

    @Column(name = "PROMO_CODE")
    private String promoCode;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "PROMO_ADDED")
    private Date promoAdded;

    @Transient
    private boolean obsolete = false;//when all items are obsolete


}
