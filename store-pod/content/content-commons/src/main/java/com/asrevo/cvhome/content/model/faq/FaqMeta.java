package com.asrevo.cvhome.content.model.faq;

import java.util.List;

/**
 * The JSON {@code meta} column of a FAQ row.
 */
public record FaqMeta(List<String> keywords, boolean showInCheckoutHelp) {
}
