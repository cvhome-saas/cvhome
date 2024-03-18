package com.asrevo.cvhome.store.entity;

import com.asrevo.cvhome.commons.domain.BaseEntity;
import com.asrevo.cvhome.storepod.commons.domain.ProductDetails;
import com.asrevo.cvhome.store.commons.domain.ProductDetailsId;
import com.asrevo.cvhome.manager.commons.domain.StoreId;
import com.asrevo.cvhome.storepod.commons.domain.ProductId;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Table("product_details")
public class ProductDetailsEntity extends BaseEntity<ProductDetailsEntity, ProductDetailsId> {
    @Column("product_details")
    private ProductDetails productDetails;
    @Column("product_id")
    private AggregateReference<ProductEntity, ProductId> product;
    private StoreId storeId;

    public static ProductDetailsEntity create(StoreId storeId, ProductId productId, ProductDetails productDetails) {
        ProductDetailsEntity productDetailsEntity = new ProductDetailsEntity();
        productDetailsEntity.setNew();
        productDetailsEntity.setStoreId(storeId);
        productDetailsEntity.setProduct(AggregateReference.to(productId));
        productDetailsEntity.setProductDetails(productDetails);
        return productDetailsEntity;
    }

    @Override
    protected ProductDetailsId generateId() {
        return ProductDetailsId.newId();
    }
}
