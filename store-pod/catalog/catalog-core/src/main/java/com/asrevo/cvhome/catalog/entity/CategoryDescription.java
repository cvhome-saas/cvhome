package com.asrevo.cvhome.catalog.entity;

import java.io.Serial;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.commons.domain.StoreScoped;
import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.common.description.BaseDescription;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "CATEGORY_DESCRIPTION",
        uniqueConstraints = @UniqueConstraint(columnNames = {"CATEGORY_ID", "LANGUAGE_CODE"}))
@SequenceGenerator(name = "description_gen", sequenceName = "category_description_seq",
        allocationSize = SchemaConstant.ID_ALLOCATION_SIZE)
@Getter
@Setter
public class CategoryDescription extends BaseDescription implements StoreScoped {

    @Serial
    private static final long serialVersionUID = 1L;

    @ManyToOne
    @JoinColumn(name = "CATEGORY_ID", nullable = false)
    private Category category;

    @Column(name = "SEF_URL", length = 120)
    private String seUrl;

    @Column(name = "CATEGORY_HIGHLIGHT")
    private String highlight;

    @Column(name = "META_TITLE", length = 120)
    private String metaTitle;

    @Column(name = "META_KEYWORDS")
    private String metaKeywords;

    @Column(name = "META_DESCRIPTION")
    private String metaDescription;

    public CategoryDescription() {
    }

    public CategoryDescription(Category category) {
        this.category = category;
    }

    @Override
    public StoreMerchantId scopedStore() {
        return category == null ? null : category.scopedStore();
    }
}
