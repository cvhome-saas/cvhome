package com.asrevo.cvhome.store.core.entity.catalog.product.type;

import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditListener;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditSection;
import com.asrevo.cvhome.store.core.entity.common.audit.Auditable;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.util.HashSet;
import java.util.Set;

@Entity
@EntityListeners(value = AuditListener.class)
@Table(name = "PRODUCT_TYPE")
@Getter
@Setter
public class ProductType extends SalesManagerEntity<Long, ProductType> implements Auditable {
    public final static String GENERAL_TYPE = "GENERAL";
    @Serial
    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "PRODUCT_TYPE_ID", unique = true, nullable = false)
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT", pkColumnValue = "PRD_TYPE_SEQ_NEXT_VAL", allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE, initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @Embedded
    private AuditSection auditSection = new AuditSection();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "productType")
    private Set<ProductTypeDescription> descriptions = new HashSet<ProductTypeDescription>();

    @Column(name = "PRD_TYPE_CODE")
    private String code;

    @Column(name = "PRD_TYPE_ADD_TO_CART")
    private Boolean allowAddToCart;

    @Column(name = "PRD_TYPE_VISIBLE")
    private Boolean visible;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MERCHANT_ID", nullable = true)
    private MerchantStore merchantStore;

    public ProductType() {
    }

}
