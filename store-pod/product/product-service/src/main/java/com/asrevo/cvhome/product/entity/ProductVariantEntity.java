package com.asrevo.cvhome.product.entity;

import com.asrevo.cvhome.commons.domain.BaseEntity;
import com.asrevo.cvhome.product.commons.domain.ProductAmount;
import com.asrevo.cvhome.product.commons.domain.ProductId;
import com.asrevo.cvhome.product.commons.domain.ProductPrice;
import com.asrevo.cvhome.product.commons.domain.ProductVariantId;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.data.relational.core.mapping.Embedded.OnEmpty.USE_NULL;

@Getter
@Setter
@Table("product_variant")
public class ProductVariantEntity extends BaseEntity<ProductVariantEntity, ProductVariantId> {
    @MappedCollection(idColumn = "product_variant_id")
    private ProductVariantRefEntity product;
    @Embedded(onEmpty = USE_NULL)
    private ProductPrice price;
    @Embedded(onEmpty = USE_NULL)
    private ProductAmount amount;
    @MappedCollection(idColumn = "product_variant_id", keyColumn = "id")
    private List<ProductVariantFeatureEntity> features = new ArrayList<>();

    public static ProductVariantEntity createProductVariant(ProductId id, ProductPrice price, ProductAmount amount, List<ProductVariantFeatureEntity> features) {
        ProductVariantEntity productVariant = new ProductVariantEntity();
        ProductVariantId generatedId = productVariant.setNew();
        productVariant.setProduct(new ProductVariantRefEntity(AggregateReference.to(id), AggregateReference.to(generatedId)));
        productVariant.setPrice(price);
        productVariant.setAmount(amount);
        productVariant.setFeatures(features);
        return productVariant;
    }

    @Override
    protected ProductVariantId generateId() {
        return ProductVariantId.newId();
    }
}
