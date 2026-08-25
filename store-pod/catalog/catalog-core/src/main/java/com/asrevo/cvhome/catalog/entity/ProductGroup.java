package com.asrevo.cvhome.catalog.entity;

import java.io.Serial;
import java.util.ArrayList;
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
 * A named set of products. Store-level groups ({@code FEATURED_ITEMS}, {@code HOME_PAGE}, …) have no parent;
 * a product's related items are the group coded {@code RELATED_ITEM} whose {@code parentProduct} is that product.
 */
@Entity
@EntityListeners(AuditListener.class)
@Table(name = "PRODUCT_GROUP", uniqueConstraints = @UniqueConstraint(columnNames = {"STORE_MERCHANT_ID", "CODE"}))
@Getter
@Setter
public class ProductGroup extends SalesManagerEntity<Long, ProductGroup> implements Auditable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "PRODUCT_GROUP_ID", unique = true, nullable = false)
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT", pkColumnValue = "PRODUCT_GROUP_SEQ_NEXT_VAL",
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

    @Column(name = "CODE", length = 100, nullable = false)
    private String code;

    @Column(name = "ACTIVE")
    private Boolean active;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_PRODUCT_ID")
    private Product parentProduct;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "PRODUCT_GROUP_PRODUCT",
            joinColumns = @JoinColumn(name = "PRODUCT_GROUP_ID"),
            inverseJoinColumns = @JoinColumn(name = "PRODUCT_ID"))
    private List<Product> products = new ArrayList<>();

    @OneToMany(mappedBy = "productGroup", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<ProductGroupDescription> descriptions = new HashSet<>();

    public ProductGroup() {
    }

    public ProductGroup(StoreMerchantId storeMerchantId, String code, Product parentProduct) {
        this.storeMerchantId = storeMerchantId;
        this.code = code;
        this.parentProduct = parentProduct;
    }

    public Optional<ProductGroupDescription> description(LanguageCode language) {
        return descriptions.stream().filter(d -> language.equals(d.getLanguageCode())).findFirst();
    }

    public boolean contains(Long productId) {
        return products.stream().anyMatch(p -> p.getId().equals(productId));
    }

    public boolean isActive() {
        return active == null ? true : active;
    }
}
