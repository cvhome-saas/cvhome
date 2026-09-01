package com.asrevo.cvhome.content.model.layout;

/**
 * What the builder loads: the current draft and where it stands. The published copy is not returned here — the
 * builder previews it through the storefront, which is the only honest renderer of it.
 */
public record ReadableLayout(LayoutDocument draft, LayoutMeta meta) {

}
