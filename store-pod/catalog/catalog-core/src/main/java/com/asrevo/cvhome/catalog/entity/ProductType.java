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
 * A merchant-defined kind of product ("shoes", "service"). Products point at it; nothing else hangs off it.
 */
@Entity
@EntityListeners(AuditListener.class)
@Table(name = "PRODUCT_TYPE")
@Getter
@Setter
/*
 * Batched at the class level so a listing page initialises every distinct type proxy in one
 * query instead of one apiece — a page showing six types issued six selects and six more for their
 * descriptions. Bounded by distinct entities rather than by rows, so it was never the worst offender,
 * but it is the same fix and the same one line.
 */
@BatchSize(size = 100)
public class ProductType extends SalesManagerEntity<Long, ProductType> implements Auditable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "PRODUCT_TYPE_ID", unique = true, nullable = false)
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT", pkColumnValue = "PRODUCT_TYPE_SEQ_NEXT_VAL",
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

    @Column(name = "PRD_TYPE_CODE")
    private String code;

    @Column(name = "PRD_TYPE_ADD_TO_CART")
    private Boolean allowAddToCart;

    @Column(name = "PRD_TYPE_VISIBLE")
    private Boolean visible;

    @OneToMany(mappedBy = "productType", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    // Batched: the type a listing row names.
    @BatchSize(size = 100)
    private Set<ProductTypeDescription> descriptions = new HashSet<>();

    public Optional<ProductTypeDescription> description(LanguageCode language) {
        return descriptions.stream().filter(d -> language.equals(d.getLanguageCode())).findFirst();
    }

    public boolean isAllowAddToCart() {
        return allowAddToCart == null ? false : allowAddToCart;
    }

    public boolean isVisible() {
        return visible == null ? false : visible;
    }
}
