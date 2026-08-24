package com.asrevo.cvhome.inventory.entity;

import java.io.Serial;
import java.time.LocalDate;
import java.util.HashSet;
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
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditSection;
import com.asrevo.cvhome.store.core.entity.common.audit.Auditable;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * One sku's stock in one store. Split out of the catalog service, so the former {@code Product} and
 * {@code ProductVariant} associations survive only as plain id columns: {@code productId} is informational (catalog
 * owns the product), and {@code productVariantId} is dormant until variants return.
 */
@Entity
@Table(name = "PRODUCT_AVAILABILITY",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"STORE_MERCHANT_ID", "PRODUCT_ID", "PRODUCT_VARIANT", "REGION_VARIANT"}),
        indexes = {@Index(name = "PRD_AVAIL_STORE_PRD_IDX", columnList = "PRODUCT_ID,STORE_MERCHANT_ID"),
                @Index(name = "PRD_AVAIL_PRD_IDX", columnList = "PRODUCT_ID"),
                @Index(name = "PRD_AVAIL_STORE_SKU_IDX", columnList = "STORE_MERCHANT_ID,SKU")})
@Getter
@Setter
public class ProductAvailability extends SalesManagerEntity<Long, ProductAvailability> implements Auditable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Embedded
    private AuditSection auditSection = new AuditSection();

    @Id
    @Column(name = "PRODUCT_AVAIL_ID", unique = true, nullable = false)
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT", pkColumnValue = "PRODUCT_AVAILABILITY_SEQ_NEXT_VAL",
            allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
            initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    /**
     * The catalog product this stock belongs to. Informational only — no foreign key, the catalog service owns it.
     */
    @Column(name = "PRODUCT_ID")
    private Long productId;

    @Embedded
    @AttributeOverride(name = "storeMerchantId",
            column = @Column(name = "STORE_MERCHANT_ID", nullable = false, length = 50))
    private StoreMerchantId storeMerchantId;

    /**
     * Dormant until variant support returns; kept so the copied rows and unique key stay intact.
     */
    @Column(name = "PRODUCT_VARIANT")
    private Long productVariantId;

    @Pattern(regexp = "^[a-zA-Z0-9_-]*$")
    @Column(name = "SKU")
    private String sku;

    @Embedded
    private ProductDimensions dimensions;

    @NotNull
    @Column(name = "QUANTITY")
    private Integer productQuantity = 0;

    @Column(name = "DATE_AVAILABLE")
    private LocalDate productDateAvailable;

    @Column(name = "REGION")
    private String region = SchemaConstant.ALL_REGIONS;

    @Column(name = "REGION_VARIANT")
    private String regionVariant;

    @Column(name = "OWNER")
    private String owner;

    @Column(name = "STATUS")
    private boolean productStatus = true; // can be used as flag for can be purchased or not

    @Column(name = "FREE_SHIPPING")
    private boolean productIsAlwaysFreeShipping;

    @Column(name = "AVAILABLE")
    private Boolean available;

    @Column(name = "QUANTITY_ORD_MIN")
    private Integer productQuantityOrderMin = 0;

    @Column(name = "QUANTITY_ORD_MAX")
    private Integer productQuantityOrderMax = 0;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "productAvailability", cascade = CascadeType.ALL)
    private Set<ProductPrice> prices = new HashSet<>();

    public ProductAvailability() {
    }

    public ProductAvailability(String sku, StoreMerchantId store) {
        this.sku = sku;
        this.storeMerchantId = store;
    }

}
