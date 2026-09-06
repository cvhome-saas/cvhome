package com.asrevo.cvhome.catalog.entity;

import java.io.Serial;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import jakarta.persistence.AttributeOverride;
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
import jakarta.persistence.UniqueConstraint;

import org.hibernate.annotations.BatchSize;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditListener;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditSection;
import com.asrevo.cvhome.store.core.entity.common.audit.Auditable;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * A store-wide option definition ("Color", "Size") — the vocabulary products vary by. Defined once per store,
 * translated once, and reused by any product that assigns it; its {@link ProductOptionValue values} carry the
 * store-wide value ids that make id-based option faceting possible.
 */
@Entity
@EntityListeners(AuditListener.class)
@Table(name = "PRODUCT_OPTION",
        uniqueConstraints = @UniqueConstraint(columnNames = {"STORE_MERCHANT_ID", "CODE"}))
@Getter
@Setter
public class ProductOption extends SalesManagerEntity<Long, ProductOption> implements Auditable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "PRODUCT_OPTION_ID", unique = true, nullable = false)
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT", pkColumnValue = "PRODUCT_OPTION_SEQ_NEXT_VAL",
            allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
            initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @Embedded
    private AuditSection auditSection = new AuditSection();

    @Embedded
    @AttributeOverride(name = "storeMerchantId",
            column = @Column(name = "STORE_MERCHANT_ID", nullable = false, length = 50))
    private StoreMerchantId storeMerchantId;

    @Column(name = "CODE", nullable = false, length = 100)
    private String code;

    @Column(name = "SORT_ORDER")
    private Integer sortOrder;

    /**
     * Batched: the product page walks every assigned option's values and their labels. Without this each option
     * paid one query for its values and each value one for its descriptions — the bulk of the ~20 statements a
     * product read used to cost. Batched, a page's options load in one query per collection.
     */
    @OneToMany(mappedBy = "option", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @BatchSize(size = 100)
    private Set<ProductOptionValue> values = new HashSet<>();

    @OneToMany(mappedBy = "option", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @BatchSize(size = 100)
    private Set<ProductOptionDescription> descriptions = new HashSet<>();

    public Optional<ProductOptionDescription> description(LanguageCode language) {
        return descriptions.stream().filter(d -> language.equals(d.getLanguageCode())).findFirst();
    }

    public Optional<ProductOptionValue> value(Long valueId) {
        return values.stream().filter(v -> v.getId().equals(valueId)).findFirst();
    }
}
