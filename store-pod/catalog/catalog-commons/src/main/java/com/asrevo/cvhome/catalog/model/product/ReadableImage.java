package com.asrevo.cvhome.catalog.model.product;

import java.io.Serial;

import com.asrevo.cvhome.commons.domain.Entity;

import lombok.Getter;
import lombok.Setter;

/**
 * A product image as read: {@code imageUrl} is where a browser fetches it from, whether that is the store's CDN
 * path or an external url.
 */
@Getter
@Setter
public class ReadableImage extends Entity {

    @Serial
    private static final long serialVersionUID = 1L;

    private String imageName;

    private String imageUrl;

    private String externalUrl;

    private String videoUrl;

    private int imageType;

    private int order;

    private boolean defaultImage;
}
