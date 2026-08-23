package com.asrevo.cvhome.content.entity;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.asrevo.cvhome.content.model.PolicyVersionStatus;

import lombok.Getter;
import lombok.Setter;

/**
 * An immutable published cut of a policy: heading and body per locale as JSON, the effective date and who
 * published it. One LIVE version per policy; publishing a new one archives the previous.
 */
@Entity
@Table(name = "POLICY_VERSION", uniqueConstraints = @UniqueConstraint(name = "policy_version_unique",
        columnNames = {"CONTENT_ID", "VERSION"}))
@Getter
@Setter
public class PolicyVersion implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(name = "content_seq", sequenceName = "content_seq", allocationSize = 50)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "content_seq")
    @Column(name = "ID")
    private Long id;

    @Column(name = "STORE_MERCHANT_ID", nullable = false, length = 50)
    private String storeMerchantId;

    @Column(name = "CONTENT_ID", nullable = false)
    private Long contentId;

    @Column(name = "VERSION", nullable = false)
    private Integer version;

    @Column(name = "STATUS", length = 12, nullable = false)
    @Enumerated(EnumType.STRING)
    private PolicyVersionStatus status = PolicyVersionStatus.LIVE;

    @Column(name = "EFFECTIVE_FROM")
    private Instant effectiveFrom;

    @Column(name = "NOTE", length = 200)
    private String note;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "TRANSLATIONS", nullable = false)
    private String translations;

    @Column(name = "PUBLISHED_AT", nullable = false)
    private Instant publishedAt = Instant.now();

    @Column(name = "PUBLISHED_BY", length = 120)
    private String publishedBy;

}
