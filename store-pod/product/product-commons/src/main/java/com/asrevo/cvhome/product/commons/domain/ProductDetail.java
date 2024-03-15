package com.asrevo.cvhome.product.commons.domain;

import java.util.List;
import java.util.Map;

public record ProductDetail(String name, String shortDescription, List<String> descriptions, Map<String, String> spec,
                            Boolean ltr) {

}
