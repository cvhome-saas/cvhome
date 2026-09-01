package com.asrevo.cvhome.content.model.storefront;

import java.util.Map;

/** One repeatable block inside a rendered section, copy already flattened to the served locale. */
public record StorefrontLayoutItem(String id, Map<String, Object> props, Map<String, String> text) {

}
