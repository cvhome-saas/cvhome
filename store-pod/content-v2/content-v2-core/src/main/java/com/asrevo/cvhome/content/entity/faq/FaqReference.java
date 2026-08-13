package com.asrevo.cvhome.content.entity.faq;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;

import com.asrevo.cvhome.content.model.faq.FaqReferenceKind;
import com.asrevo.cvhome.store.core.constants.SchemaConstant;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "FAQ_REFERENCE")
@Getter
@Setter
public class FaqReference {
    @Id
    @Column(name = "REFERENCE_ID")
    @TableGenerator(name = "faq_reference_gen", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT", pkColumnValue = "FAQ_REFERENCE_SEQ_NEXT_VAL",
            allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
            initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "faq_reference_gen")
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FAQ_CONTENT_ID", nullable = false)
    private ContentFaq faq;
    @Enumerated(EnumType.STRING)
    @Column(name = "REFERENCE_KIND", nullable = false, length = 20)
    private FaqReferenceKind referenceKind;
    @Column(name = "REFERENCE_VALUE", nullable = false, length = 255)
    private String referenceValue;
}
