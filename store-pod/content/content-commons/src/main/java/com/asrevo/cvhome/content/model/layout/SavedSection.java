package com.asrevo.cvhome.content.model.layout;

import java.time.Instant;

/**
 * A merchant-saved reusable section ("My sections" in the builder library). A snapshot, not a live reference:
 * inserting one copies it into the page with fresh ids, so editing the page never rewrites the preset and
 * deleting the preset never breaks a page.
 */
public record SavedSection(Long id, String name, String kind, LayoutSection section, Instant createdAt) {

}
