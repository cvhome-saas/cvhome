package com.asrevo.cvhome.content.model.site;

import java.io.Serializable;

/**
 * The store's brand imagery, resolved to URLs. Every slot is optional — a store that has uploaded nothing
 * renders its name as a wordmark instead.
 *
 * @param logo     the header logo
 * @param logoDark the logo used against dark backgrounds, when the store supplies a separate one
 * @param favicon  the browser-tab icon. Distinct from the logo on purpose: the two were conflated before, so a
 *                 wide wordmark ended up squeezed into a 16px tab
 * @param og       the default social-share image, used when a page supplies none of its own
 */
public record SiteBranding(MediaRef logo, MediaRef logoDark, MediaRef favicon, MediaRef og)
        implements Serializable {
}
