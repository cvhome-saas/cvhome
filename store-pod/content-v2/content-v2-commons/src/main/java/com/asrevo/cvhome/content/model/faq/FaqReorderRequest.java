package com.asrevo.cvhome.content.model.faq;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record FaqReorderRequest(@NotNull Long groupId, @NotEmpty List<@NotNull Long> orderedFaqIds) {
}
