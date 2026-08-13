package com.asrevo.cvhome.content.model.policy;

import java.time.LocalDate;
import java.util.Set;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.asrevo.cvhome.content.model.ContentWriteRequest;

public record PolicyWriteRequest(
        @Valid @NotNull ContentWriteRequest content,
        @NotNull PolicyType policyType,
        @NotBlank @Size(max = 50) String policyVersion,
        @NotNull LocalDate effectiveDate,
        boolean acceptanceRequired,
        @Size(max = 100) String jurisdiction,
        @NotEmpty Set<@NotNull PolicyDisplayLocation> displayLocations
) {
}
