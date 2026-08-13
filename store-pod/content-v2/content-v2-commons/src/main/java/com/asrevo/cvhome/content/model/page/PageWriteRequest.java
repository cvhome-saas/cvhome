package com.asrevo.cvhome.content.model.page;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.asrevo.cvhome.content.model.ContentWriteRequest;

public record PageWriteRequest(
        @NotNull @Valid ContentWriteRequest content,
        @NotBlank @Size(max = 100) String template,
        boolean showInSitemap,
        Long parentPageId,
        @NotNull @Valid List<PageBlockSpec> blocks
) {
}
