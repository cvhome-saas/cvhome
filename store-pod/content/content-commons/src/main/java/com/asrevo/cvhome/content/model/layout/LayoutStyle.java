package com.asrevo.cvhome.content.model.layout;

/**
 * The cross-kind style knobs every section carries: vertical {@code spacing} (none/sm/md/lg), {@code width}
 * (content/wide/full) and {@code tone} (default/muted/inverse). Themes interpret them loosely — they are hints,
 * not pixel values.
 */
public record LayoutStyle(String spacing, String width, String tone) {

}
