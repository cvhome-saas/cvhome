package com.asrevo.cvhome.content.entity;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.Getter;
import lombok.Setter;

/**
 * A storefront path that moved — written automatically when a published page's slug changes, so old links keep
 * resolving. Paths are stored the way the storefront builds them ({@code /content/<slug>}), without the locale.
 */
@Entity
@Table(name = "REDIRECT", uniqueConstraints = @UniqueConstraint(name = "redirect_store_from_unique",
        columnNames = {"STORE_MERCHANT_ID", "FROM_PATH"}))
@Getter
@Setter
public class Redirect implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(name = "content_seq", sequenceName = "content_seq", allocationSize = 50)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "content_seq")
    @Column(name = "ID")
    private Long id;

    @Column(name = "STORE_MERCHANT_ID", nullable = false, length = 50)
    private String storeMerchantId;

    @Column(name = "FROM_PATH", nullable = false, length = 255)
    private String fromPath;

    @Column(name = "TO_PATH", nullable = false, length = 255)
    private String toPath;

    @Column(name = "CREATED_AT", nullable = false)
    private Instant createdAt = Instant.now();

}
