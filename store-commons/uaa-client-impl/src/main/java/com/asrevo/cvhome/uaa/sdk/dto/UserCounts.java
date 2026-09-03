package com.asrevo.cvhome.uaa.sdk.dto;

/** How many accounts are in each state, counted at the moment of the call. */
public record UserCounts(long total, long active, long pending, long locked, long disabled) {
}
