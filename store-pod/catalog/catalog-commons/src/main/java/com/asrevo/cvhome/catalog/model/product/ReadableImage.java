package com.asrevo.cvhome.catalog.model.product;

import java.io.Serial;

import com.asrevo.cvhome.commons.domain.Entity;

import lombok.Getter;
import lombok.Setter;

/**
 * A product image as read: {@code imageUrl} is where a browser fetches it from, whether that is the media
 * library's public URL or an external url.
 */
@Getter
@Setter
public class ReadableImage extends Entity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * The content media asset behind this image, so a console can open it in the library. Null for external urls.
     */
    private Long mediaAssetId;

    private String imageUrl;

    private String altText;

    private String externalUrl;

    private String videoUrl;

    private int imageType;

    private int order;

    private boolean defaultImage;
}
