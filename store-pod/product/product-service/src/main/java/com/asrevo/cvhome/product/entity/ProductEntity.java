package com.asrevo.cvhome.product.entity;


import com.asrevo.cvhome.commons.domain.BaseEntity;
import com.asrevo.cvhome.product.commons.domain.*;
import com.asrevo.cvhome.product.commons.dto.CreateProductDto;
import com.asrevo.cvhome.store.commons.domain.StoreId;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.springframework.data.relational.core.mapping.Embedded.OnEmpty.USE_NULL;

@Getter
@Setter
@Table("product")
public class ProductEntity extends BaseEntity<ProductEntity, ProductId> {
    private StoreId storeId;
    @Column("category_id")
    private AggregateReference<CategoryEntity, CategoryId> category;
    private String name;
    private String description;
    @Embedded(onEmpty = USE_NULL)
    private ProductPrice price;
    private Boolean published;
    private Boolean deleted;
    @Column("image_link")
    @Embedded(onEmpty = USE_NULL)
    private ImageLink imageLink;

    public static ProductEntity createProduct(StoreId storeId, CategoryId categoryId, CreateProductDto createProductDto) {
        ProductEntity product = new ProductEntity();
        product.setNew();
        product.setStoreId(storeId);
        product.setCategory(AggregateReference.to(categoryId));
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

    public ProductEntity publish(ProductDetails pd) {
        ProductDetails productDetails = Optional.ofNullable(pd).orElseThrow(() -> new RuntimeException("product details shouldn't be null"));
        Map<DetailsLanguage, ProductDetail> details = Optional.ofNullable(productDetails.details()).orElseThrow(() -> new RuntimeException("details shouldn't be null"));
        if (details.isEmpty()) throw new RuntimeException("details should have at least one language");
        details.forEach((detailsLanguage, productDetail) -> {
            if (Objects.isNull(productDetail.ltr())) {
                throw new RuntimeException("ltr should be true or false");
            }
            if (!StringUtils.hasText(productDetail.name())) {
                throw new RuntimeException("name should be not empty");
            }
            if (!StringUtils.hasText(productDetail.shortDescription())) {
                throw new RuntimeException("short description should be not empty");
            }
            if (productDetail.descriptions() != null && !productDetail.descriptions().isEmpty()) {
                productDetail.descriptions().forEach(description -> {
                    if (!StringUtils.hasText(description)) {
                        throw new RuntimeException("description entries value should be not empty");
                    }
                });
            }
        });
        ImagesLink imageLinks = Optional.ofNullable(productDetails.extraImages()).orElseThrow(() -> new RuntimeException("imageLinks shouldn't be null"));
        if (imageLinks.isEmpty()) throw new RuntimeException("imageLinks should have at least image");
        this.published = Boolean.TRUE;
        return this;
    }

    public ProductEntity unPublish() {
        this.published = Boolean.FALSE;
        return this;
    }
}
