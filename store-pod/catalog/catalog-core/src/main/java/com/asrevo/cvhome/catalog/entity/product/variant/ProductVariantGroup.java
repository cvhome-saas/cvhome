package com.asrevo.cvhome.catalog.entity.product.variant;

import java.io.Serial;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditListener;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;

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

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "PRODUCT_VARIANT_GROUP_ID", unique = true, nullable = false)
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT", pkColumnValue = "PRODUCT_VARIANT_GROUP_SEQ_NEXT_VAL",
            allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
            initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "productVariantGroup")
    private List<ProductVariantImage> images = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY,
            cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.DETACH},
            mappedBy = "productVariantGroup")
    private Set<ProductVariant> productVariants = new HashSet<>();

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "storeMerchantId",
            column = @Column(name = "STORE_MERCHANT_ID", nullable = false, length = 50)))
    private StoreMerchantId storeMerchantId;

}
