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

/** One published version of a layout, snapshotted whole at publish time so any past home page can come back. */
@Entity
@Table(name = "PAGE_LAYOUT_REVISION")
@Getter
@Setter
public class PageLayoutRevision implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "content_seq")
    @Column(name = "ID")
    private Long id;

    @Column(name = "LAYOUT_ID", nullable = false)
    private Long layoutId;

    @Column(name = "VERSION", nullable = false)
    private int version;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "SNAPSHOT", nullable = false)
    private String snapshot;

    @Column(name = "PUBLISHED_BY", length = 120)
    private String publishedBy;

    @Column(name = "DATE_CREATED", nullable = false)
    private Instant dateCreated;

}
