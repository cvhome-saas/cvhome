package com.asrevo.cvhome.store.core.entity.catalog.catalog;


import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditListener;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditSection;
import com.asrevo.cvhome.store.core.entity.common.audit.Auditable;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * Allows grouping products and category
 * Catalog
 * - category 1
 * - category 2
 * <p>
 * - product 1
 * - product 2
 * - product 3
 * - product 4
 *
 * @author carlsamson
 */


@Entity
@EntityListeners(value = AuditListener.class)
@Table(name = "CATALOG",
        uniqueConstraints = @UniqueConstraint(columnNames = {"MERCHANT_ID", "CODE"}))
@Getter
@Setter
public class Catalog extends SalesManagerEntity<Long, Catalog> implements Auditable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE,
            generator = "TABLE_GEN")
    @TableGenerator(name = "TABLE_GEN",
            table = "SM_SEQUENCER",
            pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT",
            pkColumnValue = "CATALOG_SEQ_NEXT_VAL",
            allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
            initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE
    )
    private Long id;

    @Embedded
    private AuditSection auditSection = new AuditSection();

    @Valid
    @OneToMany(mappedBy = "catalog", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<CatalogCategoryEntry> entry = new HashSet<CatalogCategoryEntry>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MERCHANT_ID", nullable = false)
    private MerchantStore merchantStore;


    @Column(name = "VISIBLE")
    private boolean visible;

    @Column(name = "DEFAULT_CATALOG")
    private boolean defaultCatalog;

    @NotEmpty
    @Column(name = "CODE", length = 100, nullable = false)
    private String code;

    @Column(name = "SORT_ORDER")
    private Integer sortOrder = 0;

    public Catalog() {
    }

    public Catalog(MerchantStore store) {
        this.merchantStore = store;
        this.id = 0L;
    }


}