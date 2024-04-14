package com.asrevo.cvhome.store.core.entity.catalog.product.variant;

import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import com.asrevo.cvhome.store.core.entity.catalog.product.availability.ProductAvailability;
import com.asrevo.cvhome.store.core.entity.catalog.product.variation.ProductVariation;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditListener;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditSection;
import com.asrevo.cvhome.store.core.entity.common.audit.Auditable;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;


@Entity
@EntityListeners(value = AuditListener.class)
@Table(name = "PRODUCT_VARIANT",
        indexes = @Index(columnList = "PRODUCT_ID"),
        uniqueConstraints =
        @UniqueConstraint(columnNames = {
                "PRODUCT_ID",
                "SKU"}))
@Getter
@Setter
public class ProductVariant extends SalesManagerEntity<Long, ProductVariant> implements Auditable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "PRODUCT_VARIANT_ID", unique = true, nullable = false)
    @TableGenerator(name = "TABLE_GEN",
            table = "SM_SEQUENCER",
            pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT",
            pkColumnValue = "PRODUCT_VAR_SEQ_NEXT_VAL", allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE, initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;


    @Embedded
    private AuditSection auditSection = new AuditSection();

    @Column(name = "DATE_AVAILABLE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateAvailable = new Date();

    @Column(name = "AVAILABLE")
    private boolean available = true;

    @Column(name = "DEFAULT_SELECTION")
    private boolean defaultSelection = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRODUCT_VARIATION_ID", nullable = true)
    private ProductVariation variation;

    @ManyToOne(targetEntity = Product.class)
    @JoinColumn(name = "PRODUCT_ID", nullable = false)
    private Product product;

    @Column(name = "CODE", nullable = true)
    private String code;

    @Column(name = "SORT_ORDER")
    private Integer sortOrder = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRODUCT_VARIATION_VALUE_ID", nullable = true)
    private ProductVariation variationValue;

    @NotEmpty
    @Pattern(regexp = "^[a-zA-Z0-9_]*$")
    @Column(name = "SKU")
    private String sku;

    @ManyToOne(targetEntity = ProductVariantGroup.class)
    @JoinColumn(name = "PRODUCT_VARIANT_GROUP_ID", nullable = true)
    private ProductVariantGroup productVariantGroup;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "productVariant")
    private Set<ProductAvailability> availabilities = new HashSet<ProductAvailability>();

}
