package com.asrevo.cvhome.content.entity.content;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import jakarta.persistence.Version;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.errors.IllegalContentTransitionException;
import com.asrevo.cvhome.content.events.ContentLifecycleEvent;
import com.asrevo.cvhome.content.events.PolicyVersionPublishedEvent;
import com.asrevo.cvhome.content.model.ContentStatus;
import com.asrevo.cvhome.content.model.ContentType;
import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditListener;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditSection;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;

import lombok.Getter;
import lombok.Setter;

@Entity
@EntityListeners(AuditListener.class)
@Table(name = "CONTENT")
@Getter
@Setter
public class Content extends SalesManagerEntity<Long, Content> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private static final Map<ContentStatus, EnumSet<ContentStatus>> TRANSITIONS = transitions();

    @Id
    @Column(name = "CONTENT_ID", nullable = false)
    @TableGenerator(name = "content_gen", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT", pkColumnValue = "CONTENT_SEQ_NEXT_VAL",
            allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
            initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "content_gen")
    private Long id;

    @Embedded
    private AuditSection auditSection = new AuditSection();

    @Embedded
    @AttributeOverride(name = "storeMerchantId",
            column = @Column(name = "STORE_MERCHANT_ID", nullable = false, length = 50))
    private StoreMerchantId storeMerchantId;

    @Column(name = "CODE", nullable = false, length = 100)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "CONTENT_TYPE", nullable = false, length = 20)
    private ContentType contentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private ContentStatus status = ContentStatus.DRAFT;

    @Column(name = "PUBLISH_AT")
    private Instant publishAt;

    @Column(name = "UNPUBLISH_AT")
    private Instant unpublishAt;

    @Column(name = "DELETED_AT")
    private Instant deletedAt;

    @Version
    @Column(name = "VERSION", nullable = false)
    private long version;

    @OneToMany(mappedBy = "content", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ContentDescription> descriptions = new ArrayList<>();

    public void addDescription(ContentDescription description) {
        description.setContent(this);
        descriptions.add(description);
    }

    public void transition(ContentStatus target, Instant scheduledAt, Instant endAt, String actor, Instant now)
            throws IllegalContentTransitionException {
        if (!isAllowed(status, target) || target == ContentStatus.SCHEDULED
                && (scheduledAt == null || !scheduledAt.isAfter(now))) {
            throw IllegalContentTransitionException.from(status, target);
        }
        ContentStatus previous = status;
        status = target;
        publishAt = scheduledAt;
        unpublishAt = endAt;
        deletedAt = target == ContentStatus.DELETED ? now : null;
        registerEvent(new ContentLifecycleEvent(storeMerchantId, contentType, id, version, previous, target,
                actor, now));
    }

    public void policyPublished(PolicyVersionPublishedEvent event) {
        registerEvent(event);
    }

    private static boolean isAllowed(ContentStatus from, ContentStatus to) {
        return TRANSITIONS.get(from).contains(to);
    }

    private static Map<ContentStatus, EnumSet<ContentStatus>> transitions() {
        Map<ContentStatus, EnumSet<ContentStatus>> transitions = new EnumMap<>(ContentStatus.class);
        transitions.put(ContentStatus.DRAFT, EnumSet.of(ContentStatus.IN_REVIEW, ContentStatus.PUBLISHED,
                ContentStatus.SCHEDULED, ContentStatus.DELETED));
        transitions.put(ContentStatus.IN_REVIEW, EnumSet.of(ContentStatus.DRAFT, ContentStatus.PUBLISHED,
                ContentStatus.SCHEDULED, ContentStatus.DELETED));
        transitions.put(ContentStatus.SCHEDULED, EnumSet.of(ContentStatus.DRAFT, ContentStatus.PUBLISHED,
                ContentStatus.DELETED));
        transitions.put(ContentStatus.PUBLISHED, EnumSet.of(ContentStatus.UNPUBLISHED, ContentStatus.ARCHIVED,
                ContentStatus.DELETED));
        transitions.put(ContentStatus.UNPUBLISHED, EnumSet.of(ContentStatus.DRAFT, ContentStatus.PUBLISHED,
                ContentStatus.ARCHIVED, ContentStatus.DELETED));
        transitions.put(ContentStatus.ARCHIVED, EnumSet.of(ContentStatus.DRAFT, ContentStatus.DELETED));
        transitions.put(ContentStatus.DELETED, EnumSet.of(ContentStatus.DRAFT));
        return Map.copyOf(transitions);
    }
}
