package com.asrevo.cvhome.content.entity;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import lombok.Getter;
import lombok.Setter;

/** A merchant-saved reusable section: a named snapshot of one {@code LayoutSection}, copied on insert. */
@Entity
@Table(name = "SECTION_PRESET")
@Getter
@Setter
public class SectionPreset implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "content_seq")
    @Column(name = "ID")
    private Long id;

    @Column(name = "STORE_MERCHANT_ID", length = 50, nullable = false)
    private String storeMerchantId;

    @Column(name = "NAME", length = 120, nullable = false)
    private String name;

    @Column(name = "KIND", length = 40, nullable = false)
    private String kind;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "SNAPSHOT", nullable = false)
    private String snapshot;

    @Column(name = "DATE_CREATED", nullable = false)
    private Instant dateCreated;

    @Column(name = "MODIFIED_BY", length = 120)
    private String modifiedBy;

}
