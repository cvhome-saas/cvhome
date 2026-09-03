package com.asrevo.cvhome.uaa.dto;

import jakarta.validation.constraints.NotBlank;

/** The local password that confirms a brokered login may be linked to the account with the same email. */
public record LinkConfirmRequest(@NotBlank String password) {
}
