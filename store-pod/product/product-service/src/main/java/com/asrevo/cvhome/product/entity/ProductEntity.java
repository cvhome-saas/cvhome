package com.asrevo.cvhome.product.entity;


import com.asrevo.cvhome.commons.domain.BaseEntity;
import com.asrevo.cvhome.product.commons.domain.ImageLink;
import com.asrevo.cvhome.product.commons.domain.ProductId;
import com.asrevo.cvhome.product.commons.domain.ProductPrice;
import com.asrevo.cvhome.product.commons.dto.CreateProductDto;
import com.asrevo.cvhome.store.commons.domain.StoreId;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;

import static org.springframework.data.relational.core.mapping.Embedded.OnEmpty.USE_NULL;

@Getter
@Setter
@Table("product")
public class ProductEntity extends BaseEntity<ProductEntity, ProductId> {
    private StoreId storeId;
    private String name;
    private String description;
    @Embedded(onEmpty = USE_NULL)
    private ProductPrice price;
    private Boolean published;
    private Boolean deleted;
    @Column("image_link")
    @Embedded(onEmpty = USE_NULL)
    private ImageLink imageLink;

    public static ProductEntity createProduct(StoreId storeId, CreateProductDto createProductDto) {
        ProductEntity product = new ProductEntity();
        product.setNew();
        product.setStoreId(storeId);
        product.setName(createProductDto.name());
        product.setDescription(createProductDto.description());
        product.setPrice(createProductDto.price());
        product.setPublished(Boolean.FALSE);
        product.setDeleted(Boolean.FALSE);
        product.setImageLink(createProductDto.imageLink());
        return product;
    }

    @Override
    protected ProductId generateId() {
        return ProductId.newId();
    }

    public ProductEntity delete() {
        this.deleted = Boolean.TRUE;
        return this;
    }

    public ProductEntity publish() {
        this.published = Boolean.TRUE;
        return this;
    }

    public ProductEntity unPublish() {
        this.published = Boolean.FALSE;
        return this;
    }
}
