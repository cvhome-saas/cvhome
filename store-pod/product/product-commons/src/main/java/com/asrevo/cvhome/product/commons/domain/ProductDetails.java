package com.asrevo.cvhome.product.commons.domain;

import java.util.List;
import java.util.Map;

public  record ProductDetails(Map<DetailsLanguage, ProductDetail> details, List<ImageLink> extraImages) {
}

