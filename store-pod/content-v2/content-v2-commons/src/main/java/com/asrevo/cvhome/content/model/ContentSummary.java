package com.asrevo.cvhome.content.model;

import java.util.Map;

public record ContentSummary(long total, Map<ContentType, Long> byType) {
}
