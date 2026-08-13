package com.asrevo.cvhome.content.entity.page;

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
@Table(name = "PAGE_BLOCK")
@Getter
@Setter
public class PageBlock {
    @Id
    @Column(name = "BLOCK_ID")
    @TableGenerator(name = "page_block_gen", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT", pkColumnValue = "PAGE_BLOCK_SEQ_NEXT_VAL",
            allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
            initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "page_block_gen")
    private Long id;
    @ManyToOne
    @JoinColumn(name = "PAGE_CONTENT_ID", nullable = false)
    private ContentPage page;
    @Column(name = "BLOCK_TYPE", nullable = false, length = 30)
    private String blockType;
    @Column(name = "POSITION", nullable = false)
    private int position;
    @Column(name = "PAYLOAD", nullable = false, columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String payload;
}
