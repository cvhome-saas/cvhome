package com.asrevo.cvhome.content.model.post;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.asrevo.cvhome.content.model.ContentWriteRequest;

public record PostWriteRequest(
        @NotNull @Valid ContentWriteRequest content,
        @NotBlank @Size(max = 280) String excerpt,
        Long heroMediaId,
        @NotBlank @Size(max = 255) String author,
        boolean featured
) {
}
