package com.asrevo.cvhome.inventory.entity;

import java.io.Serial;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import jakarta.persistence.UniqueConstraint;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * Stock held for one order, keyed by the order's reference. Lines are the skus taken; the quantities they hold have
 * already been subtracted from {@link Inventory#getQuantity()} and go back only on release.
 */
@Entity
@Table(name = "PRODUCT_RESERVATION",
        uniqueConstraints = @UniqueConstraint(name = "UNQ_PRODUCT_RESERVATION",
                columnNames = {"STORE_MERCHANT_ID", "REF"}))
@Getter
@Setter
public class ProductReservation extends SalesManagerEntity<Long, ProductReservation> {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT", pkColumnValue = "PRODUCT_RESERVATION_SEQ_NEXT_VAL",
            allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
            initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    private Long id;

    @Embedded
    @AttributeOverride(name = "storeMerchantId",
            column = @Column(name = "STORE_MERCHANT_ID", nullable = false, length = 50))
    private StoreMerchantId storeMerchantId;

    @Column(name = "REF", nullable = false)
    private String ref;

    @Column(name = "EXPIRE_AT", nullable = false)
    private Instant expireAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false)
    private ProductReservationStatus status = ProductReservationStatus.TEMPORARY_RESERVED;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductReservationLine> lines = new ArrayList<>();

    public ProductReservation() {
    }

    public ProductReservation(StoreMerchantId storeMerchantId, String ref) {
        this.storeMerchantId = storeMerchantId;
        this.ref = ref;
    }

    public boolean holds(String sku) {
        return lines.stream().anyMatch(line -> line.getSku().equals(sku));
    }

    public void addLine(Inventory inventory, int quantity) {
        lines.add(new ProductReservationLine(this, inventory, quantity));
    }

    public boolean isExpired(Instant now) {
        return expireAt.isBefore(now);
    }
}
