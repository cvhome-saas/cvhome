package com.asrevo.cvhome.inventory.entity;

import java.io.Serial;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditSection;
import com.asrevo.cvhome.store.core.entity.common.audit.Auditable;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * One sku's stock in one store, with its prices.
 *
 * <p>
 * The table is {@code product_availability}, inherited from the catalog service, and keeps dormant columns for the
 * variant/region features that will return later; this entity maps only what the single-product model uses.
 * {@code productId} is informational — the catalog owns the product, there is no foreign key.
 * </p>
 */
@Entity
@Table(name = "PRODUCT_AVAILABILITY")
@Getter
@Setter
public class Inventory extends SalesManagerEntity<Long, Inventory> implements Auditable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "PRODUCT_AVAIL_ID", unique = true, nullable = false)
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT", pkColumnValue = "PRODUCT_AVAILABILITY_SEQ_NEXT_VAL",
            allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
            initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @Embedded
    private AuditSection auditSection = new AuditSection();

    @Embedded
    @AttributeOverride(name = "storeMerchantId",
            column = @Column(name = "STORE_MERCHANT_ID", nullable = false, length = 50))
    private StoreMerchantId storeMerchantId;

    @Column(name = "SKU")
    private String sku;

    @Column(name = "PRODUCT_ID")
    private Long productId;

    @Column(name = "QUANTITY", nullable = false)
    private int quantity;

    @Column(name = "AVAILABLE")
    private boolean available = true;

    @Column(name = "QUANTITY_ORD_MIN")
    private int quantityOrderMinimum = 1;

    /**
     * {@code 0} means no limit.
     */
    @Column(name = "QUANTITY_ORD_MAX")
    private int quantityOrderMaximum;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "inventory", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<InventoryPrice> prices = new HashSet<>();

    public Inventory() {
    }

    public Inventory(StoreMerchantId storeMerchantId, String sku) {
        this.storeMerchantId = storeMerchantId;
        this.sku = sku;
    }

    /**
     * The price the sku sells at: the one flagged default, else the oldest — legacy rows may carry several.
     */
    public Optional<InventoryPrice> defaultPrice() {
        return prices.stream().filter(InventoryPrice::isDefaultPrice).findFirst()
                .or(() -> prices.stream().min(Comparator.comparing(InventoryPrice::getId,
                        Comparator.nullsLast(Comparator.naturalOrder()))));
    }

    public boolean canBePurchased() {
        return available && quantity > 0;
    }
}
