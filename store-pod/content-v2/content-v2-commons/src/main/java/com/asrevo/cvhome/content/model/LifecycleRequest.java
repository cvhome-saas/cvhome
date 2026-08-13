package com.asrevo.cvhome.content.model;

import java.time.Instant;

import jakarta.validation.constraints.Size;

public record LifecycleRequest(Instant publishAt, Instant unpublishAt, @Size(max = 1000) String reason) {
}
