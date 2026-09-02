package com.asrevo.cvhome.uaa.dto;

/** The Users screen's tiles. */
public record UserCounts(long total, long active, long pending, long locked, long disabled) {
}
