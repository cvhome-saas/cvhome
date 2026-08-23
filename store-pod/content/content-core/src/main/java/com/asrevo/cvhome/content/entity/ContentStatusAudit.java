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

import com.asrevo.cvhome.content.model.ContentStatus;

import lombok.Getter;
import lombok.Setter;

/**
 * Append-only record of every status transition: who moved what from where to where, and why.
 */
@Entity
@Table(name = "CONTENT_STATUS_AUDIT")
@Getter
@Setter
public class ContentStatusAudit implements Serializable {

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

    @Column(name = "FROM_STATUS", length = 12)
    @Enumerated(EnumType.STRING)
    private ContentStatus fromStatus;

    @Column(name = "TO_STATUS", length = 12, nullable = false)
    @Enumerated(EnumType.STRING)
    private ContentStatus toStatus;

    @Column(name = "ACTOR", length = 120)
    private String actor;

    @Column(name = "REASON", length = 255)
    private String reason;

    @Column(name = "OCCURRED_AT", nullable = false)
    private Instant occurredAt = Instant.now();

}
