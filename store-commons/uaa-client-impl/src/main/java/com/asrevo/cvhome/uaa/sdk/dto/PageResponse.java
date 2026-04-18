package com.asrevo.cvhome.uaa.sdk.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PageResponse<T>(List<T> content, int number, int size, long totalElements, int totalPages, boolean last,
                              boolean first, boolean empty) {
}
