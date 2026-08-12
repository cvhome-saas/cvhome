package com.asrevo.cvhome.content.entity.content;

import java.io.Serial;
import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import jakarta.persistence.UniqueConstraint;

import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.common.description.BaseDescription;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "CONTENT_DESCRIPTION",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"CONTENT_ID", "LANGUAGE_ID"})})
@TableGenerator(name = "description_gen", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
        valueColumnName = "SEQ_COUNT", pkColumnValue = "CONTENT_DESCRIPTION_SEQ_NEXT_VAL",
        allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
        initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
@Getter
@Setter
public class ContentDescription extends BaseDescription implements Serializable {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    @ManyToOne(targetEntity = Content.class)
    @JoinColumn(name = "CONTENT_ID", nullable = false)
    private Content content;

    @Column(name = "SEF_URL", length = 120)
    private String seUrl;

    @Column(name = "META_KEYWORDS")
    private String metatagKeywords;

    @Column(name = "META_TITLE")
    private String metatagTitle;

    @Column(name = "META_DESCRIPTION")
    private String metatagDescription;

}
