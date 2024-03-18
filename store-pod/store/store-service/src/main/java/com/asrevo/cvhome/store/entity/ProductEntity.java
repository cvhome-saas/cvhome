package com.asrevo.cvhome.store.entity;


import com.asrevo.cvhome.commons.domain.BaseEntity;
import com.asrevo.cvhome.store.commons.domain.*;
import com.asrevo.cvhome.store.commons.dto.CreateProductDto;
import com.asrevo.cvhome.store.utils.ErrorCodes;
import com.asrevo.cvhome.commons.utils.OperationExecution;
import com.asrevo.cvhome.manager.commons.domain.StoreId;
import com.asrevo.cvhome.storepod.commons.domain.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.util.StringUtils;

import java.util.List;
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
    @Column("image_links")
    private ImagesLink imageLinks;

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
        product.setSubProducts(new SubProducts(List.of()));
        product.setImageLinks(createProductDto.imageLinks());
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
        verify(pd);
        if (subProducts != null && !subProducts.isEmpty()) {
            for (ProductDetails subProduct : subProducts) {
                verify(subProduct);
            }
        }
    }

    private void verify(ProductDetails pd) {
        ProductDetails productDetails = Optional.ofNullable(pd).orElseThrow(() -> new OperationExecution(ErrorCodes.product_details_should_not_be_null));

        ProductDetail productDetail = Optional.ofNullable(productDetails.detail()).orElseThrow(() -> new OperationExecution(ErrorCodes.details_should_not_be_null));
            if (Objects.isNull(productDetail.ltr())) {
                throw new OperationExecution(ErrorCodes.ltr_should_be_true_or_false);
            }
            if (!StringUtils.hasText(productDetail.name())) {
                throw new OperationExecution(ErrorCodes.name_should_be_not_empty);
            }
            if (!StringUtils.hasText(productDetail.shortDescription())) {
                throw new OperationExecution(ErrorCodes.short_description_should_be_not_empty);
            }
            if (productDetail.descriptions() != null && !productDetail.descriptions().isEmpty()) {
                productDetail.descriptions().forEach(description -> {
                    if (!StringUtils.hasText(description)) {
                        throw new OperationExecution(ErrorCodes.description_entries_value_should_be_not_empty);
                    }
                });
            }
        ImagesLink imageLinks = Optional.ofNullable(productDetails.extraImages()).orElseThrow(() -> new OperationExecution(ErrorCodes.imageLinks_should_not_be_null));
        if (imageLinks.isEmpty()) throw new OperationExecution(ErrorCodes.imageLinks_should_have_at_least_image);

    }

    public ProductEntity unPublish() {
        this.published = Boolean.FALSE;
        return this;
    }
}
