package com.asrevo.cvhome.checkout.entity.shoppingcart;

import java.io.Serial;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import jakarta.persistence.Transient;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditListener;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditSection;
import com.asrevo.cvhome.store.core.entity.common.audit.Auditable;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * Shopping cart is responsible for storing and carrying shopping cart
 * information.Shopping Cart consists of {@link ShoppingCartItem} which represents
 * individual lines items associated with the shopping cart
 * </p>
 *
 * @author Umesh Awasthi version 2.0
 */
@Entity
@EntityListeners(value = AuditListener.class)
@Table(name = "SHOPPING_CART",
        indexes = {@Index(name = "SHP_CART_CODE_IDX", columnList = "SHP_CART_CODE"),
                @Index(name = "SHP_CART_CUSTOMER_IDX", columnList = "CUSTOMER_ID")})
@Getter
@Setter
public class ShoppingCart extends SalesManagerEntity<Long, ShoppingCart> implements Auditable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Embedded
    private AuditSection auditSection = new AuditSection();

    @Id
    @Column(name = "SHP_CART_ID", unique = true, nullable = false)
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT", pkColumnValue = "SHOPPING_CART_SEQ_NEXT_VAL",
            allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
            initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    /**
     * Will be used to fetch shopping cart model from the controller this is a unique code
     * that should be attributed from the client (UI)
     */
    @Column(name = "SHP_CART_CODE", unique = true, nullable = false)
    private String shoppingCartCode;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "shoppingCart")
    private Set<ShoppingCartItem> lineItems = new HashSet<>();

    @Embedded
    @AttributeOverride(name = "storeMerchantId",
            column = @Column(name = "STORE_MERCHANT_ID", nullable = false, length = 50))
    private StoreMerchantId storeMerchantId;

    @Column(name = "CUSTOMER_ID")
    private Long customerId;

    @Column(name = "ORDER_ID")
    private Long orderId;

    @Column(name = "IP_ADDRESS")
    private String ipAddress;

    @Column(name = "PROMO_CODE")
    private String promoCode;

    @Column(name = "PROMO_ADDED")
    private Instant promoAdded;

    @Transient
    private boolean obsolete = false; // when all items are obsolete

}
