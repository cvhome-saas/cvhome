package com.asrevo.cvhome.product.commons.dto;

import com.asrevo.cvhome.product.commons.domain.ProductAmount;
import com.asrevo.cvhome.product.commons.domain.ProductId;
import com.asrevo.cvhome.product.commons.domain.ProductPrice;
import com.asrevo.cvhome.product.commons.domain.ProductVariantId;

public record ProductVariantDto(ProductVariantId id, ProductId productId, ProductPrice price, ProductAmount amount) {
}
