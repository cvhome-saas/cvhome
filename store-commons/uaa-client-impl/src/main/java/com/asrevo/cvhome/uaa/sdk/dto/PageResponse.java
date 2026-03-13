package com.asrevo.cvhome.uaa.sdk.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PageResponse<T>(List<T> content, int number, int size, long totalElements, int totalPages, boolean last,
		boolean first, boolean empty) {
}
