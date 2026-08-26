package com.asrevo.cvhome.content.model.section;

import com.asrevo.cvhome.content.model.HomeSectionKind;
import com.asrevo.cvhome.content.model.menu.MenuTarget;

/**
 * The non-queried half of a home section, held in the content row's {@code meta} JSON.
 *
 * @param kind        what the section renders
 * @param targetValue the product group code, category code, banner slug or FAQ group key it points at
 * @param mediaId     the image for an {@code IMAGE} section
 * @param itemLimit   how many items to draw, for the collection kinds; {@code null} means the theme decides
 * @param layout      a theme-interpreted hint such as {@code grid} or {@code carousel}
 * @param cta         an optional call to action below the block
 */
public record SectionMeta(HomeSectionKind kind, String targetValue, Long mediaId, Integer itemLimit, String layout,
                          MenuTarget cta) {
}
