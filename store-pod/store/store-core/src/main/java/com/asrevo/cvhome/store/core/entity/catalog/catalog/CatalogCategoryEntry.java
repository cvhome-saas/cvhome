package com.asrevo.cvhome.store.core.entity.catalog.catalog;

import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.catalog.category.Category;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditListener;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditSection;
import com.asrevo.cvhome.store.core.entity.common.audit.Auditable;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

@Entity
@EntityListeners(value = AuditListener.class)
@Table(name = "CATALOG_ENTRY", uniqueConstraints =
@UniqueConstraint(columnNames = {"CATEGORY_ID", "CATALOG_ID"}))
@Getter
@Setter
public class CatalogCategoryEntry extends SalesManagerEntity<Long, CatalogCategoryEntry> implements Auditable {


    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;
    @ManyToOne
    @JoinColumn(name = "CATEGORY_ID", nullable = false)
    Category category;
    @Embedded
    private AuditSection auditSection = new AuditSection();
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE,
            generator = "TABLE_GEN")

    @TableGenerator(name = "TABLE_GEN",
            table = "SM_SEQUENCER",
            pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT",
            allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
            initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE,
            pkColumnValue = "CATALOG_ENT_SEQ_NEXT_VAL")
    private Long id;
    @ManyToOne
    @JoinColumn(name = "CATALOG_ID", nullable = false)
    private Catalog catalog;

    //TODO d products ????

    @Column(name = "VISIBLE")
    private boolean visible;

}
