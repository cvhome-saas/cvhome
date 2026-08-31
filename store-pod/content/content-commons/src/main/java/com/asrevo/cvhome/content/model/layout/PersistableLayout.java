package com.asrevo.cvhome.content.model.layout;

import jakarta.validation.constraints.NotNull;

/**
 * A draft save. {@code baseVersion} is the {@code draftVersion} the builder loaded; a mismatch on write means
 * someone else saved in between and the request is rejected with a conflict instead of silently clobbering.
 */
public record PersistableLayout(@NotNull LayoutDocument document, @NotNull Integer baseVersion) {

}
