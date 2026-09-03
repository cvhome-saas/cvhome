package com.asrevo.cvhome.uaa.dto;

/** Signed in and linked: where the browser should go next — the saved request, or the console. */
public record LinkConfirmResponse(String username, String redirectTo) {
}
