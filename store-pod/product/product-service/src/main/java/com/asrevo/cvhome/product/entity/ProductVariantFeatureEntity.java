package com.asrevo.cvhome.product.entity;

import com.asrevo.cvhome.commons.domain.BaseEntity;
import com.asrevo.cvhome.product.commons.domain.ProductFeatureId;
import com.asrevo.cvhome.product.commons.domain.ProductId;
import com.asrevo.cvhome.store.commons.domain.StoreId;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Table("product_variant_feature")
public class ProductVariantFeatureEntity extends BaseEntity<ProductVariantFeatureEntity, ProductFeatureId> {
    @Column("product_id")
    private AggregateReference<ProductEntity, ProductId> product;
    private StoreId storeId;
    private String key;
    private String value;

    public static ProductVariantFeatureEntity createProductFeature(ProductId productId, StoreId storeId, String key, String value) {
        ProductVariantFeatureEntity feature = new ProductVariantFeatureEntity();
        feature.setNew();
        feature.setProduct(AggregateReference.to(productId));
        feature.setStoreId(storeId);
        feature.setKey(key);
        feature.setValue(value);
        return feature;
    }

    @Override
    protected ProductFeatureId generateId() {
        return ProductFeatureId.newId();
    }
}
