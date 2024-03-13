package com.asrevo.cvhome.product.entity;

import com.asrevo.cvhome.commons.domain.BaseEntity;
import com.asrevo.cvhome.product.commons.domain.ProductFeatureId;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Table("product_variant_feature")
public class ProductVariantFeatureEntity extends BaseEntity<ProductVariantFeatureEntity, ProductFeatureId> {
  private String key;
  private String value;

  public static ProductVariantFeatureEntity createProductFeature(String key, String value) {
    ProductVariantFeatureEntity feature = new ProductVariantFeatureEntity();
    feature.setNew();
    feature.setKey(key);
    feature.setValue(value);
    return feature;
  }

  @Override
  protected ProductFeatureId generateId() {
    return ProductFeatureId.newId();
  }
}
