package com.asrevo.cvhome.product.entity;

import com.asrevo.cvhome.product.commons.domain.ProductId;
import com.asrevo.cvhome.product.commons.domain.ProductVariantId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("product_variant_ref")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantRefEntity {
  @Column("product_id")
  private AggregateReference<ProductEntity, ProductId> product;
  @Column("product_variant_id")
  private AggregateReference<ProductVariantEntity, ProductVariantId> productVariant;

}
