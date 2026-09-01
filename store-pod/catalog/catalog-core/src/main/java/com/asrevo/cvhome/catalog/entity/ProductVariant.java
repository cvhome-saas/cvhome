package com.asrevo.cvhome.catalog.entity;

import java.io.Serial;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import jakarta.persistence.UniqueConstraint;

import org.hibernate.annotations.BatchSize;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditListener;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditSection;
import com.asrevo.cvhome.store.core.entity.common.audit.Auditable;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * The sellable unit: every product owns at least one variant, and the sku — the cross-service key to price and
 * stock in the inventory service — always lives here.
 *
 * <p>
 * A product with no assigned options owns exactly one <em>default</em> variant with the
 * {@link #DEFAULT_SIGNATURE} signature and no option values. A product with options owns one variant per sold
 * combination; {@code optionSignature} is the combination's canonical form (sorted option-value ids joined with
 * {@code -}) and is unique per product, which is what makes duplicate combinations impossible under concurrency.
 * Sellability (price, stock, availability) is entirely the inventory service's, keyed by sku — this row carries
 * no availability flag on purpose.
 * </p>
 */
@Entity
@EntityListeners(AuditListener.class)
@Table(name = "PRODUCT_VARIANT", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"STORE_MERCHANT_ID", "SKU"}),
        @UniqueConstraint(columnNames = {"PRODUCT_ID", "OPTION_SIGNATURE"})})
@Getter
@Setter
public class ProductVariant extends SalesManagerEntity<Long, ProductVariant> implements Auditable {

    /**
     * The signature of the one variant a no-options product owns.
     */
    public static final String DEFAULT_SIGNATURE = "DEFAULT";

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "PRODUCT_VARIANT_ID", unique = true, nullable = false)
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT", pkColumnValue = "PRODUCT_VARIANT_SEQ_NEXT_VAL",
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRODUCT_ID", nullable = false)
    private Product product;

    @Column(name = "SKU", nullable = false)
    private String sku;

    @Column(name = "SORT_ORDER")
    private Integer sortOrder;

    @Column(name = "DEFAULT_VARIANT", nullable = false)
    private boolean defaultVariant;

    @Column(name = "OPTION_SIGNATURE", nullable = false)
    private String optionSignature = DEFAULT_SIGNATURE;

    /*
     * Batched: the PDP and the console matrix fetch-join these, but anything else iterating a product's
     * variants would otherwise pay one query per variant.
     */
    @OneToMany(mappedBy = "variant", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @BatchSize(size = 100)
    private Set<ProductVariantOptionValue> optionValues = new HashSet<>();

    public ProductVariant() {
    }

    public ProductVariant(Product product, String sku) {
        this.product = product;
        this.storeMerchantId = product.getStore();
        this.sku = sku;
    }

    public int getSortOrder() {
        return sortOrder == null ? 0 : sortOrder;
    }

    /**
     * The combination's option-value ids, ascending — the order the signature uses.
     */
    public List<Long> optionValueIds() {
        return optionValues.stream().map(v -> v.getOptionValue().getId()).sorted().toList();
    }

    /**
     * The canonical signature of a combination: sorted value ids joined with {@code -}, or
     * {@link #DEFAULT_SIGNATURE} for no values at all.
     */
    public static String signatureOf(Collection<Long> optionValueIds) {
        if (optionValueIds == null || optionValueIds.isEmpty()) {
            return DEFAULT_SIGNATURE;
        }
        return optionValueIds.stream().sorted().map(String::valueOf).collect(Collectors.joining("-"));
    }
}
