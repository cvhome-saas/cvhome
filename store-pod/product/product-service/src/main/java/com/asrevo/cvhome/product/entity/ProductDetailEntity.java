package com.asrevo.cvhome.product.entity;


import com.asrevo.cvhome.commons.domain.BaseEntity;
import com.asrevo.cvhome.product.commons.domain.ProductDetailId;
import com.asrevo.cvhome.product.commons.domain.ProductId;
import com.asrevo.cvhome.product.commons.dto.CreateProductDetailDto;
import com.asrevo.cvhome.store.commons.domain.StoreId;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Table("product_detail")
public class ProductDetailEntity extends BaseEntity<ProductDetailEntity, ProductDetailId> {
    private StoreId storeId;
    @Column("product_id")
    private AggregateReference<ProductEntity, ProductId> product;

    public static ProductDetailEntity createProductDetail(StoreId storeId, ProductId productId, CreateProductDetailDto createProductDetailDto) {
        ProductDetailEntity product = new ProductDetailEntity();
        product.setNew();
        product.setStoreId(storeId);
        product.setProduct(AggregateReference.to(productId));
        return product;
    }

    @Override
    protected ProductDetailId generateId() {
        return ProductDetailId.newId();
    }

}
