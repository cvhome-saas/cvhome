package com.asrevo.cvhome.content.entity.content;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.asrevo.cvhome.store.core.constants.SchemaConstant;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "CONTENT_REVISION")
@Getter
@Setter
public class ContentRevision {
    @Id
    @Column(name = "REVISION_ID")
    @TableGenerator(name = "revision_gen", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT", pkColumnValue = "CONTENT_REVISION_SEQ_NEXT_VAL",
            allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
            initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "revision_gen")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "CONTENT_ID", nullable = false)
    private Content content;

    @Column(name = "VERSION", nullable = false)
    private long version;

    @Column(name = "SNAPSHOT", nullable = false, columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String snapshot;

    @Column(name = "AUTHOR", nullable = false)
    private String author;

    @Column(name = "CREATED_AT", nullable = false)
    private Instant createdAt;
}
