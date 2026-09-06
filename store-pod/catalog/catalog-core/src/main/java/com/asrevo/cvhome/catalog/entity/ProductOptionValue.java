package com.asrevo.cvhome.catalog.entity;

import java.io.Serial;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import jakarta.persistence.UniqueConstraint;

import org.hibernate.annotations.BatchSize;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditListener;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditSection;
import com.asrevo.cvhome.store.core.entity.common.audit.Auditable;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * One value of a store option ("Red" of "Color"). Scoped to its option — codes are unique within the option, and
 * tenancy comes through the owning {@link ProductOption}.
 */
@Entity
@EntityListeners(AuditListener.class)
@Table(name = "PRODUCT_OPTION_VALUE",
        uniqueConstraints = @UniqueConstraint(columnNames = {"PRODUCT_OPTION_ID", "CODE"}))
@Getter
@Setter
public class ProductOptionValue extends SalesManagerEntity<Long, ProductOptionValue> implements Auditable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "PRODUCT_OPTION_VALUE_ID", unique = true, nullable = false)
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT", pkColumnValue = "PRODUCT_OPTION_VALUE_SEQ_NEXT_VAL",
            allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
            initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @Embedded
    private AuditSection auditSection = new AuditSection();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRODUCT_OPTION_ID", nullable = false)
    private ProductOption option;

    @Column(name = "CODE", nullable = false, length = 100)
    private String code;

    @Column(name = "SORT_ORDER")
    private Integer sortOrder;

    // Batched for the same reason as ProductOption.values: a label per value was one query per value.
    @OneToMany(mappedBy = "optionValue", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @BatchSize(size = 100)
    private Set<ProductOptionValueDescription> descriptions = new HashSet<>();

    public ProductOptionValue() {
    }

    public ProductOptionValue(ProductOption option) {
        this.option = option;
    }

    public Optional<ProductOptionValueDescription> description(LanguageCode language) {
        return descriptions.stream().filter(d -> language.equals(d.getLanguageCode())).findFirst();
    }
}
