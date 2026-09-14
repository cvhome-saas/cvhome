package com.asrevo.cvhome.catalog.entity;

import java.io.Serial;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.common.description.BaseDescription;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "PRODUCT_OPTION_DESCRIPTION",
        uniqueConstraints = @UniqueConstraint(columnNames = {"PRODUCT_OPTION_ID", "LANGUAGE_CODE"}))
@SequenceGenerator(name = "description_gen", sequenceName = "product_option_description_seq",
        allocationSize = SchemaConstant.ID_ALLOCATION_SIZE)
@Getter
@Setter
public class ProductOptionDescription extends BaseDescription {

    @Serial
    private static final long serialVersionUID = 1L;

    @ManyToOne
    @JoinColumn(name = "PRODUCT_OPTION_ID", nullable = false)
    private ProductOption option;

    public ProductOptionDescription() {
    }

    public ProductOptionDescription(ProductOption option) {
        this.option = option;
    }
}
