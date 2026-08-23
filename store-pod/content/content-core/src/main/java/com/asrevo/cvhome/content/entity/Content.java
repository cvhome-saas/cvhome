package com.asrevo.cvhome.content.entity;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.model.BannerPlacement;
import com.asrevo.cvhome.content.model.ContentStatus;
import com.asrevo.cvhome.content.model.PageTemplate;
import com.asrevo.cvhome.content.model.PolicyType;
import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditListener;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditSection;
import com.asrevo.cvhome.store.core.entity.content.ContentPosition;
import com.asrevo.cvhome.store.core.entity.content.ContentType;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * One CMS item of any type — the legacy {@code content} table, carried forward and extended. Legacy {@code BOX} and
 * {@code PAGE} rows keep their columns and meaning; the workflow columns ({@code status}, {@code publishAt},
 * {@code version} …) and the type-specific ones ({@code placement}, {@code policyType}, {@code template},
 * {@code meta}) were added with the content platform and all default so an existing row stays valid.
 */
@Entity
@EntityListeners(value = AuditListener.class)
@Table(name = "CONTENT", indexes = {@Index(name = "CODE_IDX", columnList = "CODE")},
        uniqueConstraints = @UniqueConstraint(columnNames = {"STORE_MERCHANT_ID", "CODE"}))
@Getter
@Setter
public class Content extends SalesManagerEntity<Long, Content> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1772757159185494620L;

    @Id
    @Column(name = "CONTENT_ID", unique = true, nullable = false)
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT", pkColumnValue = "CONTENT_SEQ_NEXT_VAL",
            allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
            initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @Embedded
    private AuditSection auditSection = new AuditSection();

    @Valid
    @OneToMany(mappedBy = "content", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ContentDescription> descriptions = new ArrayList<>();

    @Embedded
    @AttributeOverride(name = "storeMerchantId",
            column = @Column(name = "STORE_MERCHANT_ID", nullable = false, length = 50))
    private StoreMerchantId storeMerchantId;

    /**
     * The slug (pages, posts, FAQ, policies, banners) or the snippet code (BOX rows). Unique per store.
     */
    @NotEmpty
    @Column(name = "CODE", length = 100, nullable = false)
    private String code;

    /**
     * Legacy visibility. Kept in step with {@link #status} for workflow rows ({@code visible == PUBLISHED}) and the
     * only switch for BOX rows.
     */
    @Column(name = "VISIBLE")
    private boolean visible;

    @Column(name = "LINK_TO_MENU")
    private boolean linkToMenu;

    @Column(name = "CONTENT_POSITION", length = 10)
    @Enumerated(value = EnumType.STRING)
    private ContentPosition contentPosition;

    @Column(name = "CONTENT_TYPE", length = 10)
    @Enumerated(value = EnumType.STRING)
    private ContentType contentType;

    @Column(name = "SORT_ORDER")
    private Integer sortOrder = 0;

    @Column(name = "PRODUCT_GROUP")
    private String productGroup;

    // --- workflow columns (content platform) ---

    @Column(name = "STATUS", length = 12, nullable = false)
    @Enumerated(value = EnumType.STRING)
    private ContentStatus status = ContentStatus.DRAFT;

    @Column(name = "PUBLISH_AT")
    private Instant publishAt;

    @Column(name = "UNPUBLISH_AT")
    private Instant unpublishAt;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version;

    @Column(name = "CREATED_BY", length = 120)
    private String createdBy;

    @Column(name = "UPDATED_BY", length = 120)
    private String updatedBy;

    @Column(name = "PARENT_ID")
    private Long parentId;

    @Column(name = "TEMPLATE", length = 20)
    @Enumerated(value = EnumType.STRING)
    private PageTemplate template;

    @Column(name = "NOINDEX", nullable = false)
    private boolean noindex;

    @Column(name = "CANONICAL_URL", length = 500)
    private String canonicalUrl;

    @Column(name = "OG_MEDIA_ID")
    private Long ogMediaId;

    @Column(name = "SHOW_IN_FOOTER", nullable = false)
    private boolean showInFooter;

    @Column(name = "PLACEMENT", length = 20)
    @Enumerated(value = EnumType.STRING)
    private BannerPlacement placement;

    @Column(name = "STARTS_AT")
    private Instant startsAt;

    @Column(name = "ENDS_AT")
    private Instant endsAt;

    @Column(name = "POLICY_TYPE", length = 20)
    @Enumerated(value = EnumType.STRING)
    private PolicyType policyType;

    /**
     * Type-specific payload nothing queries on, as a JSON document (post hero/tags, banner artwork/target, FAQ
     * keywords, policy jurisdiction…). Page rows reserve {@code blocks} here for the future builder.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "META")
    private String meta;

    public ContentDescription getDescription() {
        if (this.getDescriptions() != null && !this.getDescriptions().isEmpty()) {
            return this.getDescriptions().getFirst();
        }
        return null;
    }

    public Optional<ContentDescription> description(LanguageCode language) {
        if (language == null || descriptions == null) {
            return Optional.empty();
        }
        return descriptions.stream().filter(d -> language.equals(d.getLanguageCode())).findFirst();
    }

    /**
     * Whether the storefront may serve this row right now: a BOX/SECTION row when visible, a workflow row when
     * {@code PUBLISHED} and inside its window.
     */
    public boolean servable(Instant now) {
        if (contentType == null || !contentType.workflow()) {
            return visible;
        }
        if (status != ContentStatus.PUBLISHED) {
            return false;
        }
        if (publishAt != null && publishAt.isAfter(now)) {
            return false;
        }
        return unpublishAt == null || unpublishAt.isAfter(now);
    }

}
