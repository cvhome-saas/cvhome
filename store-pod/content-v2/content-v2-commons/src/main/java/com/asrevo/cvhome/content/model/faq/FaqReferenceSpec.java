package com.asrevo.cvhome.content.model.faq;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record FaqReferenceSpec(
        @NotNull FaqReferenceKind kind,
        @NotBlank @Size(max = 255) String value
) {
}
