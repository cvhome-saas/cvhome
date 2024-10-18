package com.asrevo.cvhome.store.core.entity.catalog.product.variant;

import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditListener;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import jakarta.persistence.*;
import java.io.Serial;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

/**
 * Extra properties on a group of variants
 *
 * @author carlsamson
 */
@Entity
@EntityListeners(value = AuditListener.class)
@Table(name = "PRODUCT_VARIANT_GROUP")
@Getter
@Setter
public class ProductVariantGroup extends SalesManagerEntity<Long, ProductVariantGroup> {

    @Serial private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "PRODUCT_VARIANT_GROUP_ID", unique = true, nullable = false)
    @TableGenerator(
            name = "TABLE_GEN",
            table = "SM_SEQUENCER",
            pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT",
            pkColumnValue = "PRODUCT_VAR_GROUP_SEQ_NEXT_VAL",
            allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
            initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "productVariantGroup")
    private List<ProductVariantImage> images = new ArrayList<>();

    @OneToMany(
            fetch = FetchType.LAZY,
            cascade = {
                CascadeType.MERGE,
                CascadeType.PERSIST,
                CascadeType.REFRESH,
                CascadeType.DETACH
            },
            mappedBy = "productVariantGroup")
    private Set<ProductVariant> productVariants = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MERCHANT_ID", nullable = false)
    private MerchantStore merchantStore;
}
