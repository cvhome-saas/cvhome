package com.asrevo.cvhome.content.model.layout;

import java.util.List;

import com.asrevo.cvhome.errors.FieldError;

/**
 * The publish response: the new state plus warnings that did not block — a section pointing at a product group
 * or category this service cannot see is reported here, because the storefront tolerates a missing reference at
 * render (the section collapses) and catalog data is not content's to assert.
 */
public record PublishedLayout(LayoutMeta meta, List<FieldError> warnings) {

}
