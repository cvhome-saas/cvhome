package com.asrevo.cvhome.content.model.storefront;

import java.util.List;
import java.util.Map;

import com.asrevo.cvhome.content.model.layout.LayoutStyle;

/** One renderable block: resolved copy in one locale, props with media urls resolved, device visibility kept. */
public record StorefrontLayoutSection(String id, String kind, String variant, Map<String, Object> props,
                                      List<StorefrontLayoutItem> items, Map<String, String> text,
                                      LayoutStyle style, String anchor, List<String> devices) {

}
