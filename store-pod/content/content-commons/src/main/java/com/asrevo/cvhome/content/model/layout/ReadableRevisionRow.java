package com.asrevo.cvhome.content.model.layout;

import java.time.Instant;

/** One published version in the layout's history, listed newest first. Restoring writes it into the draft. */
public record ReadableRevisionRow(int version, Instant publishedAt, String publishedBy) {

}
