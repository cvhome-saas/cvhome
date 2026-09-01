package com.asrevo.cvhome.content.model.layout;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Save one configured section under a name for reuse. */
public record PersistableSavedSection(@NotBlank @Size(max = 120) String name, @NotNull LayoutSection section) {

}
