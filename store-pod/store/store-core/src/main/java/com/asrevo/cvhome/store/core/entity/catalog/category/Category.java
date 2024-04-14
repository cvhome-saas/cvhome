package com.asrevo.cvhome.store.core.entity.catalog.category;

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

import java.io.Serial;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@EntityListeners(value = AuditListener.class)
@Table(name = "CATEGORY",
        indexes = @Index(columnList = "LINEAGE"),
        uniqueConstraints =
        @UniqueConstraint(columnNames = {"MERCHANT_ID", "CODE"}))

@Getter
@Setter

public class Category extends SalesManagerEntity<Long, Category> implements Auditable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "CATEGORY_ID", unique = true, nullable = false)
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT", pkColumnValue = "CATEGORY_SEQ_NEXT_VAL", allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE, initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @Embedded
    private AuditSection auditSection = new AuditSection();

    @Valid
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<CategoryDescription> descriptions = new HashSet<CategoryDescription>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MERCHANT_ID", nullable = false)
    private MerchantStore merchantStore;

    @ManyToOne
    @JoinColumn(name = "PARENT_ID")
    private Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.REMOVE)
    private List<Category> categories = new ArrayList<Category>();

    @Column(name = "CATEGORY_IMAGE", length = 100)
    private String categoryImage;

    @Column(name = "SORT_ORDER")
    private Integer sortOrder = 0;

    @Column(name = "CATEGORY_STATUS")
    private boolean categoryStatus;

    @Column(name = "VISIBLE")
    private boolean visible;

    @Column(name = "DEPTH")
    private Integer depth;

    @Column(name = "LINEAGE")
    private String lineage;

    @Column(name = "FEATURED")
    private boolean featured;

    @NotEmpty
    @Column(name = "CODE", length = 100, nullable = false)
    private String code;

    public Category() {
    }

    public Category(MerchantStore store) {
        this.merchantStore = store;
        this.id = 0L;
    }

    public CategoryDescription getDescription() {
        if (descriptions != null && !descriptions.isEmpty()) {
            return descriptions.iterator().next();
        }

        return null;
    }
}