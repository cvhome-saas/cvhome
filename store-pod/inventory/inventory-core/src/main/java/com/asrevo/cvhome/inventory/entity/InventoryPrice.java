package com.asrevo.cvhome.inventory.entity;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * A price on an {@link Inventory} row: the regular amount plus an optional special amount with a date window.
 *
 * <p>
 * Table {@code product_price}, inherited from the catalog service. Its {@code product_price_code} and
 * {@code default_price} columns exist for the multi-price model; the single-product model writes one {@code base}
 * default price per sku and reads the default one back.
 * </p>
 */
@Entity
@Table(name = "PRODUCT_PRICE")
@Getter
@Setter
public class InventoryPrice extends SalesManagerEntity<Long, InventoryPrice> {

    public static final String DEFAULT_CODE = "base";

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "PRODUCT_PRICE_ID")
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT", pkColumnValue = "PRODUCT_PRICE_SEQ_NEXT_VAL",
            allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
            initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @Embedded
    @AttributeOverride(name = "storeMerchantId",
            column = @Column(name = "STORE_MERCHANT_ID", length = 50))
    private StoreMerchantId storeMerchantId;

    @ManyToOne
    @JoinColumn(name = "PRODUCT_AVAIL_ID", nullable = false)
    private Inventory inventory;

    @Column(name = "PRODUCT_PRICE_CODE", nullable = false)
    private String code = DEFAULT_CODE;

    @Column(name = "DEFAULT_PRICE")
    private boolean defaultPrice = true;

    @Column(name = "PRODUCT_PRICE_AMOUNT")
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(name = "PRODUCT_PRICE_SPECIAL_AMOUNT")
    private BigDecimal specialAmount;

    @Column(name = "PRODUCT_PRICE_SPECIAL_ST_DATE")
    private LocalDate specialStartDate;

    @Column(name = "PRODUCT_PRICE_SPECIAL_END_DATE")
    private LocalDate specialEndDate;

    public InventoryPrice() {
    }

    public InventoryPrice(Inventory inventory) {
        this.inventory = inventory;
        this.storeMerchantId = inventory.getStoreMerchantId();
    }

    /**
     * Whether the special amount applies on {@code day}: it must be set and positive, and the day must fall inside
     * the window — from the start date (inclusive, or open) up to the end date (exclusive, or open).
     */
    public boolean isSpecialActiveOn(LocalDate day) {
        boolean hasSpecial = specialAmount != null && specialAmount.signum() > 0;
        boolean started = specialStartDate == null || !specialStartDate.isAfter(day);
        boolean notEnded = specialEndDate == null || specialEndDate.isAfter(day);
        return hasSpecial && started && notEnded;
    }
}
