package com.asrevo.cvhome.catalog.entity.product.availability;

import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.entity.product.ProductDimensions;
import com.asrevo.cvhome.catalog.entity.product.price.ProductPrice;
import com.asrevo.cvhome.catalog.entity.product.variant.ProductVariant;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditSection;
import com.asrevo.cvhome.store.core.entity.common.audit.Auditable;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import com.asrevo.cvhome.store.core.utils.CloneUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.io.Serial;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "PRODUCT_AVAILABILITY",
        uniqueConstraints =
                @UniqueConstraint(
                        columnNames = {
                            "STORE_MERCHANT_ID",
                            "PRODUCT_ID",
                            "PRODUCT_VARIANT",
                            "REGION_VARIANT"
                        }),
        indexes = {
            @Index(name = "PRD_AVAIL_STORE_PRD_IDX", columnList = "PRODUCT_ID,STORE_MERCHANT_ID"),
            @Index(name = "PRD_AVAIL_PRD_IDX", columnList = "PRODUCT_ID")
        })
@Getter
@Setter
public class ProductAvailability extends SalesManagerEntity<Long, ProductAvailability>
        implements Auditable {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    @Embedded private AuditSection auditSection = new AuditSection();

    @Id
    @Column(name = "PRODUCT_AVAIL_ID", unique = true, nullable = false)
    @TableGenerator(
            name = "TABLE_GEN",
            table = "SM_SEQUENCER",
            pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT",
            pkColumnValue = "PRODUCT_AVAILABILITY_SEQ_NEXT_VAL",
            allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
            initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @JsonIgnore
    @ManyToOne(targetEntity = Product.class)
    @JoinColumn(name = "PRODUCT_ID", nullable = false)
    private Product product;

    @Embedded
    @AttributeOverrides(
            @AttributeOverride(
                    name = "storeMerchantId",
                    column = @Column(name = "STORE_MERCHANT_ID", nullable = false, length = 50)))
    private StoreMerchantId storeMerchantId;

    /**
     * This describes the availability of a product variant
     */
    @ManyToOne(targetEntity = ProductVariant.class)
    @JoinColumn(name = "PRODUCT_VARIANT")
    private ProductVariant productVariant;

    @Pattern(regexp = "^[a-zA-Z0-9_]*$")
    @Column(name = "SKU")
    private String sku;

    @Embedded private ProductDimensions dimensions;

    @NotNull @Column(name = "QUANTITY")
    private Integer productQuantity = 0;

    @Temporal(TemporalType.DATE)
    @Column(name = "DATE_AVAILABLE")
    private Date productDateAvailable;

    @Column(name = "REGION")
    private String region = SchemaConstant.ALL_REGIONS;

    @Column(name = "REGION_VARIANT")
    private String regionVariant;

    @Column(name = "OWNER")
    private String owner;

    @Column(name = "STATUS")
    private boolean productStatus = true; // can be used as flag for variant can be purchase or not

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

    public ProductAvailability() {}

    public ProductAvailability(Product product, StoreMerchantId store) {
        this.product = product;
        this.storeMerchantId = store;
    }

    public Date getProductDateAvailable() {
        return CloneUtils.clone(productDateAvailable);
    }

    public void setProductDateAvailable(Date productDateAvailable) {
        this.productDateAvailable = CloneUtils.clone(productDateAvailable);
    }
}
