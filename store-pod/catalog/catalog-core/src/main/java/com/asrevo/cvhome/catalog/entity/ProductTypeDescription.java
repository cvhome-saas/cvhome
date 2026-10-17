package com.asrevo.cvhome.catalog.entity;

import java.io.Serial;

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
@Table(name = "PRODUCT_TYPE_DESCRIPTION",
        uniqueConstraints = @UniqueConstraint(columnNames = {"PRODUCT_TYPE_ID", "LANGUAGE_CODE"}))
@SequenceGenerator(name = "description_gen", sequenceName = "product_type_description_seq",
        allocationSize = SchemaConstant.ID_ALLOCATION_SIZE)
@Getter
@Setter
public class ProductTypeDescription extends BaseDescription implements StoreScoped {

    @Serial
    private static final long serialVersionUID = 1L;

    @ManyToOne
    @JoinColumn(name = "PRODUCT_TYPE_ID", nullable = false)
    private ProductType productType;

    public ProductTypeDescription() {
    }

    public ProductTypeDescription(ProductType productType) {
        this.productType = productType;
    }

    @Override
    public StoreMerchantId scopedStore() {
        return productType == null ? null : productType.scopedStore();
    }
}
