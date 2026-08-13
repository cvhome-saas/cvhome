package com.asrevo.cvhome.content.model.banner;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BannerArtworkSpec(
        Long desktopMediaId,
        Long mobileMediaId,
        @NotBlank @Size(max = 500) String altText
) {
}
