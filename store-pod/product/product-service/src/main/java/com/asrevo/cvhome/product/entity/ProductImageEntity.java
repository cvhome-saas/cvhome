package com.asrevo.cvhome.product.entity;

import com.asrevo.cvhome.commons.domain.BaseEntity;
import com.asrevo.cvhome.product.commons.domain.ImageLink;
import com.asrevo.cvhome.product.commons.domain.ProductId;
import com.asrevo.cvhome.product.commons.domain.ProductImageId;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;

import static org.springframework.data.relational.core.mapping.Embedded.OnEmpty.USE_NULL;

@Getter
@Setter
@Table("product_image")
public class ProductImageEntity extends BaseEntity<ProductImageEntity, ProductImageId> {
    @Column("link")
    @Embedded(onEmpty = USE_NULL)
    private ImageLink link;
    @Column("product_id")
    private AggregateReference<ProductEntity, ProductId> product;

    public static ProductImageEntity create(ProductId productId, ImageLink link) {
        ProductImageEntity productImage = new ProductImageEntity();
        productImage.setNew();
        productImage.setProduct(AggregateReference.to(productId));
        productImage.setLink(link);
        return productImage;
    }

    @Override
    protected ProductImageId generateId() {
        return ProductImageId.newId();
    }
}
