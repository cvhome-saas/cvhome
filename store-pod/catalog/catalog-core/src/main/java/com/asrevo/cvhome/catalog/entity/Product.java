package com.asrevo.cvhome.catalog.entity;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import jakarta.persistence.UniqueConstraint;

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
 * A sellable item's catalog data: sku, copy per language, images, brand, type, categories and the shipping box.
 * Price and stock live in the inventory service, keyed by {@code sku}.
 *
 * <p>
 * The {@code product} table keeps columns of features that are not modelled here (condition, rental, reviews,
 * attributes); they stay unmapped until those features return.
 * </p>
 */
@Entity
@EntityListeners(AuditListener.class)
@Table(name = "PRODUCT", uniqueConstraints = @UniqueConstraint(columnNames = {"STORE_MERCHANT_ID", "SKU"}))
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

    @Column(name = "SKU")
    private String sku;

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
}
