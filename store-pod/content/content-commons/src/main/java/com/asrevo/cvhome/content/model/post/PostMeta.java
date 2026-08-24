package com.asrevo.cvhome.content.model.post;

import java.util.List;

/**
 * The JSON {@code meta} column of a POST row.
 */
public record PostMeta(Long heroMediaId, List<Long> categoryIds, List<String> tags, String authorName,
                       boolean featured) {
}
