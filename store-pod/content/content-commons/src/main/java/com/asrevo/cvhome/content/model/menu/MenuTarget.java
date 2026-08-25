package com.asrevo.cvhome.content.model.menu;

import com.asrevo.cvhome.content.model.MenuTargetKind;

/**
 * What a menu item points at. {@code value} is a page slug, category/product handle, policy type, or URL.
 * {@code broken} is set by the server when an internal target no longer resolves.
 */
public record MenuTarget(MenuTargetKind kind, String value, Boolean broken) {
}
