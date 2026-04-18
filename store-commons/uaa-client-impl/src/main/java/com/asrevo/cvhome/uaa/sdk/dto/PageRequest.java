package com.asrevo.cvhome.uaa.sdk.dto;

public record PageRequest(int page, int size) {
    public static PageRequest of(int page, int size) {
        return new PageRequest(page, size);
    }
}
