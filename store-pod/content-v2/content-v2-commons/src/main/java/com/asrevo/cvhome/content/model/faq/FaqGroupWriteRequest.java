package com.asrevo.cvhome.content.model.faq;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record FaqGroupWriteRequest(
        @NotBlank @Size(max = 100) String code,
        @NotBlank @Size(max = 255) String name,
        @PositiveOrZero int position
) {
}
