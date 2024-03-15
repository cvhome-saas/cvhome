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

import java.util.List;
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
    @Embedded(onEmpty = USE_NULL)
    private ProductAmount amount;
    @Column("product_type")
    private ProductType productType;
    @Column("sub_products")
    private SubProducts subProducts;

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
        product.setAmount(createProductDto.amount());
        product.setProductType(createProductDto.productType());
        product.setSubProducts(createProductDto.subProducts());
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

    public ProductEntity publish(ProductDetails pd, List<ProductDetails> subProducts) {
        verify(pd, subProducts);
        this.published = Boolean.TRUE;
        return this;
    }

    public void verify(ProductDetails pd, List<ProductDetails> subProducts) {
        verify(pd, "master");
        if (subProducts != null && !subProducts.isEmpty()) {
            for (ProductDetails subProduct : subProducts) {
                verify(subProduct, "sub");
            }
        }
    }

    private void verify(ProductDetails pd, String product) {
        ProductDetails productDetails = Optional.ofNullable(pd).orElseThrow(() -> new RuntimeException(product + " product details shouldn't be null"));
        Map<DetailsLanguage, ProductDetail> details = Optional.ofNullable(productDetails.details()).orElseThrow(() -> new RuntimeException(product + " details shouldn't be null"));
        if (details.isEmpty()) throw new RuntimeException(product + " details should have at least one language");
        details.forEach((detailsLanguage, productDetail) -> {
            if (Objects.isNull(productDetail.ltr())) {
                throw new RuntimeException(product + " ltr should be true or false");
            }
            if (!StringUtils.hasText(productDetail.name())) {
                throw new RuntimeException(product + " name should be not empty");
            }
            if (!StringUtils.hasText(productDetail.shortDescription())) {
                throw new RuntimeException(product + " short description should be not empty");
            }
            if (productDetail.descriptions() != null && !productDetail.descriptions().isEmpty()) {
                productDetail.descriptions().forEach(description -> {
                    if (!StringUtils.hasText(description)) {
                        throw new RuntimeException(product + " description entries value should be not empty");
                    }
                });
            }
        });
        ImagesLink imageLinks = Optional.ofNullable(productDetails.extraImages()).orElseThrow(() -> new RuntimeException("imageLinks shouldn't be null"));
        if (imageLinks.isEmpty()) throw new RuntimeException("imageLinks should have at least image");

    }

    public ProductEntity unPublish() {
        this.published = Boolean.FALSE;
        return this;
    }
}
