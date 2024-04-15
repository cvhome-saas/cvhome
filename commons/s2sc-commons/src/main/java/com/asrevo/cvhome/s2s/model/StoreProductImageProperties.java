package com.asrevo.cvhome.s2s.model;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("com.asrevo.cvhome.store.product.props")
public record StoreProductImageProperties(boolean cropUploads, String height, String width, String maxHeight,
                                          String maxWidth, String formats) {
}
