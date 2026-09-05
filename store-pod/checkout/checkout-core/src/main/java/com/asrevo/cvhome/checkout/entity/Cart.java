package com.asrevo.cvhome.checkout.entity;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import jakarta.persistence.Version;

import com.asrevo.cvhome.checkout.domain.CartCode;
import com.asrevo.cvhome.checkout.entity.converter.CartCodeConverter;
import com.asrevo.cvhome.checkout.model.cart.CartStatus;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.converter.LanguageCodeConverter;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditListener;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditSection;
import com.asrevo.cvhome.store.core.entity.common.audit.Auditable;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * A shopper's cart: sku and quantity per line, nothing else. Prices are read live from inventory on every read, so a
 * cart never carries a stale price.
 */
@Entity
@EntityListeners(AuditListener.class)
@Table(name = "CART")
@Getter
@Setter
public class Cart extends SalesManagerEntity<Long, Cart> implements Auditable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "CART_ID")
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT", pkColumnValue = "CART_SEQ_NEXT_VAL",
            allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
            initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    /** Two checkouts of one cart racing each other: the second loses here instead of making a second order. */
    @Version
    @Column(name = "VERSION", nullable = false)
    private long version;

    @Embedded
    private AuditSection auditSection = new AuditSection();

    @Embedded
    @AttributeOverride(name = "storeMerchantId",
            column = @Column(name = "STORE_MERCHANT_ID", nullable = false, length = 50))
    private StoreMerchantId storeMerchantId;

    @Column(name = "CART_CODE", nullable = false, unique = true, length = 36)
    @Convert(converter = CartCodeConverter.class)
    private CartCode code;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 10)
    private CartStatus status = CartStatus.ACTIVE;

    @Column(name = "ORDER_ID")
    private Long orderId;

    @Column(name = "CUA_EXTERNAL_ID", length = 96)
    private String cuaExternalId;

    @Column(name = "LANGUAGE_CODE", length = 6)
    @Convert(converter = LanguageCodeConverter.class)
    private LanguageCode language;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartLine> lines = new ArrayList<>();

    public Cart() {
    }

    public Cart(StoreMerchantId storeMerchantId, CartCode code, LanguageCode language) {
        this.storeMerchantId = storeMerchantId;
        this.code = code;
        this.language = language;
    }

    public Optional<CartLine> line(String sku) {
        return lines.stream().filter(line -> line.getSku().equals(sku)).findFirst();
    }

    /**
     * Sets the line for {@code sku} to exactly {@code quantity}; zero removes it.
     */
    public void put(String sku, int quantity) {
        Optional<CartLine> existing = line(sku);
        if (quantity <= 0) {
            existing.ifPresent(lines::remove);
            return;
        }
        if (existing.isPresent()) {
            existing.get().setQuantity(quantity);
        } else {
            lines.add(new CartLine(this, sku, quantity));
        }
    }

    public void remove(String sku) {
        lines.removeIf(line -> line.getSku().equals(sku));
    }

    public boolean isActive() {
        return status == CartStatus.ACTIVE;
    }

    public void convertedInto(Long newOrderId) {
        this.status = CartStatus.CONVERTED;
        this.orderId = newOrderId;
    }

    public void reopen() {
        this.status = CartStatus.ACTIVE;
        this.orderId = null;
    }
}
