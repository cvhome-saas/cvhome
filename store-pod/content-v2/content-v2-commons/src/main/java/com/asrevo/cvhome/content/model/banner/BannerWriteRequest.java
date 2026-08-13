package com.asrevo.cvhome.content.model.banner;

import java.util.Set;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import com.asrevo.cvhome.content.model.ContentWriteRequest;

public record BannerWriteRequest(
        @Valid @NotNull ContentWriteRequest content,
        @NotNull BannerPlacement placement,
        @PositiveOrZero int position,
        @NotNull BannerTargetKind targetKind,
        @Size(max = 1000) String targetValue,
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$") String backgroundColor,
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$") String foregroundColor,
        @NotNull LoginTarget loginTarget,
        @Size(max = 50) Set<@Pattern(regexp = "^[A-Z]{2}$") String> countryCodes,
        @Valid @NotNull BannerArtworkSpec artwork
) {
}
