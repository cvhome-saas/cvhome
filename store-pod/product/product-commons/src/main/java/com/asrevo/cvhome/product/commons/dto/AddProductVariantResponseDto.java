package com.asrevo.cvhome.product.commons.dto;

import com.asrevo.cvhome.product.commons.domain.ProductAmount;
import com.asrevo.cvhome.product.commons.domain.ProductPrice;
import com.asrevo.cvhome.product.commons.domain.ProductVariantId;

import java.util.Map;

public record AddProductVariantResponseDto(ProductVariantId id,ProductPrice price, ProductAmount amount, Map<String, String> features) {
}
