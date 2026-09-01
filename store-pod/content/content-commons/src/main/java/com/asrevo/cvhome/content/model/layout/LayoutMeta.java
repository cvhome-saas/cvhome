package com.asrevo.cvhome.content.model.layout;

import java.time.Instant;

/**
 * Where a layout stands: the draft's optimistic-lock version (sent back on every save and publish), the version
 * currently live (null when never published), when it went live, and whether the draft differs from what
 * shoppers see.
 */
public record LayoutMeta(int draftVersion, Integer publishedVersion, Instant publishedAt, boolean dirty) {

}
