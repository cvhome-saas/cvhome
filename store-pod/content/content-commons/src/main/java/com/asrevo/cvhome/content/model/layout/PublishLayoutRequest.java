package com.asrevo.cvhome.content.model.layout;

import jakarta.validation.constraints.NotNull;

/** Publish (or discard) against the draft version the builder holds, so a stale client cannot ship a surprise. */
public record PublishLayoutRequest(@NotNull Integer baseVersion) {

}
