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
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
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
 * A node of the store's category tree. The tree is materialised in {@code lineage} ({@code /1/7/} — every ancestor
 * id, then the node's own) and {@code depth}, which is what makes "the whole subtree" one {@code like} query.
 */
@Entity
@EntityListeners(AuditListener.class)
@Table(name = "CATEGORY", indexes = @Index(columnList = "LINEAGE"),
        uniqueConstraints = @UniqueConstraint(columnNames = {"STORE_MERCHANT_ID", "CODE"}))
@Getter
@Setter
public class Category extends SalesManagerEntity<Long, Category> implements Auditable {

    public static final String PATH_SEPARATOR = "/";

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "CATEGORY_ID", unique = true, nullable = false)
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT", pkColumnValue = "CATEGORY_SEQ_NEXT_VAL",
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

    @ManyToOne
    @JoinColumn(name = "PARENT_ID")
    private Category parent;

    @Column(name = "SORT_ORDER")
    private Integer sortOrder;

    @Column(name = "VISIBLE")
    private Boolean visible;

    @Column(name = "FEATURED")
    private Boolean featured;

    @Column(name = "DEPTH")
    private Integer depth;

    @Column(name = "LINEAGE")
    private String lineage;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<CategoryDescription> descriptions = new HashSet<>();

    public Optional<CategoryDescription> description(LanguageCode language) {
        return descriptions.stream().filter(d -> language.equals(d.getLanguageCode())).findFirst();
    }

    /**
     * Places this node under {@code newParent} (null for the root) and recomputes its path.
     */
    public void placeUnder(Category newParent) {
        this.parent = newParent;
        this.depth = newParent == null ? 0 : newParent.getDepth() + 1;
        this.lineage = (newParent == null ? PATH_SEPARATOR : newParent.getLineage()) + id + PATH_SEPARATOR;
    }

    /**
     * The prefix every descendant's lineage starts with.
     */
    public String subtreePrefix() {
        return lineage;
    }

    public int getSortOrder() {
        return sortOrder == null ? 0 : sortOrder;
    }

    public boolean isVisible() {
        return visible == null ? false : visible;
    }

    public boolean isFeatured() {
        return featured == null ? false : featured;
    }

    public int getDepth() {
        return depth == null ? 0 : depth;
    }
}
