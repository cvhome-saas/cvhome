package com.asrevo.cvhome.uaa.dto;

import jakarta.validation.constraints.NotBlank;

/** The password chosen on an accept page. Goes through the realm's policy like every other password. */
public record AcceptLinkRequest(@NotBlank String password) {
}
