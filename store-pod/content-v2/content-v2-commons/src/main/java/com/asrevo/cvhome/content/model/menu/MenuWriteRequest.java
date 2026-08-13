package com.asrevo.cvhome.content.model.menu;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.asrevo.cvhome.content.model.ContentWriteRequest;

public record MenuWriteRequest(
        @Valid @NotNull ContentWriteRequest content,
        @NotBlank @Size(max = 100) String handle,
        @Valid @Size(max = 100) List<MenuItemSpec> items
) {
}
