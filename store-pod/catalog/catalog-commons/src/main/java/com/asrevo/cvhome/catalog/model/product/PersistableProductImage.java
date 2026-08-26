package com.asrevo.cvhome.catalog.model.product;

import java.io.Serial;
import java.io.Serializable;

import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

/**
 * One image to attach to a product: either a media asset the seller picked from the content library, or an
 * external url.
 *
 * <p>
 * Catalog no longer accepts file uploads. Bytes go to the media library, which dedupes them, reads their
 * dimensions, tracks what uses them and refuses to delete one a product still holds; catalog stores the id.
 * </p>
 */
@Getter
@Setter
public class PersistableProductImage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** The media asset to attach. Mutually exclusive with {@link #externalUrl}. */
    private Long mediaAssetId;

    @Size(max = 500)
    private String externalUrl;

    @Size(max = 500)
    private String videoUrl;

    /** Overrides the asset's own alt text for this product. */
    @Size(max = 255)
    private String altText;

    private boolean defaultImage;

}
