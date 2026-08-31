package com.asrevo.cvhome.catalog.entity;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import jakarta.persistence.Transient;

import org.hibernate.annotations.BatchSize;
import org.springframework.data.domain.AfterDomainEventPublication;

import com.asrevo.cvhome.catalog.model.product.event.ProductSearchIndexPurgedEvent;
import com.asrevo.cvhome.catalog.model.product.event.ProductSearchIndexStaleEvent;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditListener;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditSection;
import com.asrevo.cvhome.store.core.entity.common.audit.Auditable;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * A product's pure definition: copy per language, images, brand, type, categories and the shipping box. The
 * sellable unit is always a {@link ProductVariant} — every product owns at least one — and price and stock live
 * in the inventory service, keyed by the variant's sku.
 *
 * <p>
 * The {@code product} table keeps columns of features that are not modelled here (condition, rental, reviews);
 * they stay unmapped until those features return.
 * </p>
 */
@Entity
@EntityListeners(AuditListener.class)
@Table(name = "PRODUCT")
@Getter
@Setter
public class Product extends SalesManagerEntity<Long, Product> implements Auditable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "PRODUCT_ID", unique = true, nullable = false)
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT", pkColumnValue = "PRODUCT_SEQ_NEXT_VAL",
            allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
            initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @Embedded
    private AuditSection auditSection = new AuditSection();

    @Embedded
    @AttributeOverride(name = "storeMerchantId",
            column = @Column(name = "STORE_MERCHANT_ID", nullable = false, length = 50))
    private StoreMerchantId store;

    /**
     * The merchandising switch: whether the storefront shows the product at all.
     */
    @Column(name = "AVAILABLE")
    private Boolean available;

    @Column(name = "DATE_AVAILABLE")
    private Instant dateAvailable = Instant.now();

    @Column(name = "PRODUCT_VIRTUAL")
    private Boolean productVirtual;

    @Column(name = "PRODUCT_SHIP")
    private Boolean productShipeable;

    @Column(name = "SORT_ORDER")
    private Integer sortOrder;

    @Column(name = "PRODUCT_LENGTH")
    private BigDecimal length;

    @Column(name = "PRODUCT_WIDTH")
    private BigDecimal width;

    @Column(name = "PRODUCT_HEIGHT")
    private BigDecimal height;

    @Column(name = "PRODUCT_WEIGHT")
    private BigDecimal weight;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MANUFACTURER_ID")
    private Manufacturer manufacturer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRODUCT_TYPE_ID")
    private ProductType type;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "PRODUCT_CATEGORY", joinColumns = @JoinColumn(name = "PRODUCT_ID"),
            inverseJoinColumns = @JoinColumn(name = "CATEGORY_ID"))
    private Set<Category> categories = new HashSet<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<ProductDescription> descriptions = new HashSet<>();

    /**
     * Not cascaded on save: an image row only exists once its file has been stored, which the image service does.
     * Removed with the product.
     */
    @OneToMany(mappedBy = "product", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private Set<ProductImage> images = new HashSet<>();

    /**
     * The sellable units — never empty on a persisted product. Managed by the variant service (the whole set is
     * replaced atomically together with {@link #optionAssignments}); batched so a listing page loads every row's
     * variants in one IN query instead of one per product.
     */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @BatchSize(size = 100)
    private Set<ProductVariant> variants = new HashSet<>();

    /**
     * The ordered axes this product varies by — empty for a simple product.
     */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @BatchSize(size = 100)
    private Set<ProductOptionAssignment> optionAssignments = new HashSet<>();

    /**
     * Whether this product owes the search index an update. Not persisted, and not part of the product's shape:
     * it lives only for as long as it takes the repository to publish the event.
     */
    @Transient
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private boolean searchIndexStale;

    @Transient
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private boolean searchIndexPurged;

    public Optional<ProductDescription> description(LanguageCode language) {
        return descriptions.stream().filter(d -> language.equals(d.getLanguageCode())).findFirst();
    }

    /**
     * The image flagged default, else the first by sort order.
     */
    public Optional<ProductImage> defaultImage() {
        return images.stream().filter(ProductImage::isDefaultImage).findFirst()
                .or(() -> images.stream().min(Comparator.comparingInt(ProductImage::getSortOrder)));
    }

    /**
     * The variant flagged default — a partial unique index guarantees at most one — else the first by sort
     * order then id, so the answer is deterministic even on unflagged legacy data.
     */
    public Optional<ProductVariant> defaultVariant() {
        return variants.stream().filter(ProductVariant::isDefaultVariant).findFirst()
                .or(() -> variants.stream().min(Comparator.comparingInt(ProductVariant::getSortOrder)
                        .thenComparing(ProductVariant::getId,
                                Comparator.nullsLast(Comparator.naturalOrder()))));
    }

    public boolean isAvailable() {
        return available == null ? true : available;
    }

    public boolean isProductVirtual() {
        return productVirtual == null ? false : productVirtual;
    }

    public boolean isProductShipeable() {
        return productShipeable == null ? false : productShipeable;
    }

    public int getSortOrder() {
        return sortOrder == null ? 0 : sortOrder;
    }

    /**
     * Something searchable about this product changed — its copy, sku, brand or categories.
     *
     * <p>
     * The event is only handed to the outbox when the aggregate goes through the repository, so a caller that
     * mutates a managed product and relies on dirty checking has to {@code save} it afterwards even though the
     * row would have been written anyway.
     * </p>
     */
    public Product searchIndexStale() {
        this.searchIndexStale = true;
        return this;
    }

    /**
     * This product is being deleted; its index rows go with it.
     */
    public Product searchIndexPurged() {
        this.searchIndexPurged = true;
        return this;
    }

    /**
     * Built here rather than at the call site because a newly created product has no id until the insert has run,
     * and Spring Data reads this after the repository call — so the event carries the real id, and a caller does
     * not have to save twice to get one.
     */
    @Override
    protected Collection<Object> domainEvents() {
        if (!searchIndexStale && !searchIndexPurged) {
            return super.domainEvents();
        }
        List<Object> events = new ArrayList<>(super.domainEvents());
        if (searchIndexPurged) {
            events.add(ProductSearchIndexPurgedEvent.from(id, store.getId()));
        } else {
            events.add(ProductSearchIndexStaleEvent.from(id, store.getId()));
        }
        return events;
    }

    @Override
    @AfterDomainEventPublication
    protected void clearDomainEvents() {
        super.clearDomainEvents();
        this.searchIndexStale = false;
        this.searchIndexPurged = false;
    }
}
