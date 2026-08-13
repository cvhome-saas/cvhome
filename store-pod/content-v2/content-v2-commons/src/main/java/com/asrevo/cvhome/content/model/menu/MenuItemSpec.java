package com.asrevo.cvhome.content.model.menu;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MenuItemSpec(
        @NotBlank @Size(max = 255) String label,
        @NotNull MenuTargetKind targetKind,
        @NotBlank @Size(max = 1000) String targetValue,
        boolean openNewTab,
        boolean visible,
        boolean loginRequired,
        @Valid @NotNull @Size(max = 100) List<MenuItemSpec> children
) {
}
