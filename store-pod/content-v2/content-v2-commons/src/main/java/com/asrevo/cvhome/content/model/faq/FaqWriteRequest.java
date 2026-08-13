package com.asrevo.cvhome.content.model.faq;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import com.asrevo.cvhome.content.model.ContentWriteRequest;

public record FaqWriteRequest(
        @Valid @NotNull ContentWriteRequest content,
        @NotNull Long groupId,
        @PositiveOrZero int position,
        @Valid @Size(max = 50) List<FaqReferenceSpec> references
) {
}
