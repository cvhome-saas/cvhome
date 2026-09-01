package com.asrevo.cvhome.content.model.layout;

import java.util.Map;

/**
 * One repeatable block inside a section — a hero slide, a USP badge, a testimonial quote. Same conventions as
 * the section it belongs to: {@code props} for machine settings ({@code mediaId} keys are media refs),
 * {@code text} for localized copy.
 */
public record LayoutItem(String id, Map<String, Object> props, Map<String, Map<String, String>> text) {

    public Map<String, Object> props() {
        return props == null ? Map.of() : props;
    }

    public Map<String, Map<String, String>> text() {
        return text == null ? Map.of() : text;
    }

}
