package com.asrevo.cvhome.content.entity.faq;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.converter.LanguageCodeConverter;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "FAQ_GROUP_DESCRIPTION")
@Getter
@Setter
public class FaqGroupDescription {
    @Id
    @Column(name = "GROUP_DESCRIPTION_ID")
    @TableGenerator(name = "faq_group_description_gen", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT", pkColumnValue = "FAQ_GROUP_DESCRIPTION_SEQ_NEXT_VAL",
            allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
            initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "faq_group_description_gen")
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "GROUP_ID", nullable = false)
    private FaqGroup group;
    @Column(name = "LANGUAGE_CODE", length = 6)
    @Convert(converter = LanguageCodeConverter.class)
    private LanguageCode languageCode;
    @Column(name = "NAME", nullable = false, length = 255)
    private String name;
}
