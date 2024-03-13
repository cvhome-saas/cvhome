package com.asrevo.cvhome.product.commons.dto;

import com.asrevo.cvhome.product.commons.domain.ProductAmount;
import com.asrevo.cvhome.product.commons.domain.ProductPrice;

import java.util.Map;

public record AddProductVariantDto(ProductPrice price, ProductAmount amount, Map<String, String> features) {
}
