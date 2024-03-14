package com.asrevo.cvhome.product.entity;

import com.asrevo.cvhome.commons.domain.BaseEntity;
import com.asrevo.cvhome.product.commons.domain.ProductAmount;
import com.asrevo.cvhome.product.commons.domain.ProductId;
import com.asrevo.cvhome.product.commons.domain.ProductPrice;
import com.asrevo.cvhome.product.commons.domain.ProductVariantId;
import com.asrevo.cvhome.product.commons.dto.AddProductVariantDto;
import com.asrevo.cvhome.store.commons.domain.StoreId;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Map;

import static org.springframework.data.relational.core.mapping.Embedded.OnEmpty.USE_NULL;

@Getter
@Setter
@Table("product_variant")
public class ProductVariantEntity extends BaseEntity<ProductVariantEntity, ProductVariantId> {
    @Column("product_id")
    private AggregateReference<ProductEntity, ProductId> product;
    private StoreId storeId;
    @Embedded(onEmpty = USE_NULL)
    private ProductPrice price;
    @Embedded(onEmpty = USE_NULL)
    private ProductAmount amount;
    private Map<String, String> features;

    public static ProductVariantEntity createProductVariant(ProductId productId, StoreId storeId, AddProductVariantDto addProductVariantDto) {
        ProductVariantEntity productVariant = new ProductVariantEntity();
        productVariant.setNew();
        productVariant.setProduct(AggregateReference.to(productId));
        productVariant.setStoreId(storeId);
        productVariant.setPrice(addProductVariantDto.price());
        productVariant.setAmount(addProductVariantDto.amount());
        productVariant.setFeatures(addProductVariantDto.features());
        return productVariant;
    }

    @Override
    protected ProductVariantId generateId() {
        return ProductVariantId.newId();
    }
}
