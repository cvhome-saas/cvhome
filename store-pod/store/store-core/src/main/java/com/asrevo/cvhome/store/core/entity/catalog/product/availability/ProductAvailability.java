package com.asrevo.cvhome.store.core.entity.catalog.product.availability;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import com.asrevo.cvhome.store.core.entity.catalog.product.ProductDimensions;
import com.asrevo.cvhome.store.core.entity.catalog.product.price.ProductPrice;
import com.asrevo.cvhome.store.core.entity.catalog.product.variant.ProductVariant;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditSection;
import com.asrevo.cvhome.store.core.entity.common.audit.Auditable;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.utils.CloneUtils;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "PRODUCT_AVAILABILITY",
        uniqueConstraints = @UniqueConstraint(columnNames = {"MERCHANT_ID", "PRODUCT_ID", "PRODUCT_VARIANT", "REGION_VARIANT"}),
        indexes =
                {
                        @Index(name = "PRD_AVAIL_STORE_PRD_IDX", columnList = "PRODUCT_ID,MERCHANT_ID"),
                        @Index(name = "PRD_AVAIL_PRD_IDX", columnList = "PRODUCT_ID")
                }
)

/**
 * Default availability
 *
 * store
 * product id
 *
 * variant null
 * regionVariant null
 *
 * @author carlsamson
 *
 */
@Getter
@Setter
public class ProductAvailability extends SalesManagerEntity<Long, ProductAvailability> implements Auditable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Embedded
    private AuditSection auditSection = new AuditSection();

    @Id
    @Column(name = "PRODUCT_AVAIL_ID", unique = true, nullable = false)
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT", pkColumnValue = "PRODUCT_AVAIL_SEQ_NEXT_VAL")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @JsonIgnore
    @ManyToOne(targetEntity = Product.class)
    @JoinColumn(name = "PRODUCT_ID", nullable = false)
    private Product product;

    /**
     * Specific retailer store
     **/
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MERCHANT_ID", nullable = true)
    private MerchantStore merchantStore;

    /**
     * This describes the availability of a product variant
     */
    @ManyToOne(targetEntity = ProductVariant.class)
    @JoinColumn(name = "PRODUCT_VARIANT", nullable = true)
    private ProductVariant productVariant;

    @Pattern(regexp = "^[a-zA-Z0-9_]*$")
    @Column(name = "SKU", nullable = true)
    private String sku;

    @Embedded
    private ProductDimensions dimensions;

    @NotNull
    @Column(name = "QUANTITY")
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
    private boolean productStatus = true; //can be used as flag for variant can be purchase or not

    @Column(name = "FREE_SHIPPING")
    private boolean productIsAlwaysFreeShipping;

    @Column(name = "AVAILABLE")
    private Boolean available;

    @Column(name = "QUANTITY_ORD_MIN")
    private Integer productQuantityOrderMin = 0;

    @Column(name = "QUANTITY_ORD_MAX")
    private Integer productQuantityOrderMax = 0;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "productAvailability", cascade = CascadeType.ALL)
    private Set<ProductPrice> prices = new HashSet<ProductPrice>();


    public ProductAvailability() {
    }

    public ProductAvailability(Product product, MerchantStore store) {
        this.product = product;
        this.merchantStore = store;
    }

    @Transient
    public ProductPrice defaultPrice() {
        for (ProductPrice price : prices) {
            if (price.isDefaultPrice()) {
                return price;
            }
        }
        return new ProductPrice();
    }


    public Date getProductDateAvailable() {
        return CloneUtils.clone(productDateAvailable);
    }

    public void setProductDateAvailable(Date productDateAvailable) {
        this.productDateAvailable = CloneUtils.clone(productDateAvailable);
    }


}
