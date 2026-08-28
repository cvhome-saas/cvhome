package com.asrevo.cvhome.catalog.entity;

import java.io.Serial;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;

import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * One image of a product.
 *
 * <p>
 * {@code imageType} 0 is an asset in the content service's media library, held as {@code mediaAssetId} with its
 * public URL cached alongside; {@code imageType} 1 is an external url (possibly a video). Catalog used to store
 * the file itself under the product's sku, which gave product images no alt text, no metadata and no way for
 * anything to know an image was still in use.
 * </p>
 *
 * <p>
 * The cached URL is safe to hold because an asset's bytes are never replaced in place: an upload either
 * deduplicates onto the existing asset or mints a new id. It can only go stale if the asset is deleted, which
 * the usage index refuses while a product still references it.
 * </p>
 */
@Entity
@Table(name = "PRODUCT_IMAGE")
@Getter
@Setter
public class ProductImage extends SalesManagerEntity<Long, ProductImage> {

    public static final int TYPE_MEDIA_ASSET = 0;

    public static final int TYPE_EXTERNAL_URL = 1;

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "PRODUCT_IMAGE_ID")
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT", pkColumnValue = "PRODUCT_IMAGE_SEQ_NEXT_VAL",
            allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
            initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "PRODUCT_ID", nullable = false)
    private Product product;

    /**
     * The content media asset this image is, and the source of truth for it.
     */
    @Column(name = "MEDIA_ASSET_ID")
    private Long mediaAssetId;

    /**
     * The asset's public URL, cached so reading a product needs no call into content.
     */
    @Column(name = "IMAGE_URL", length = 500)
    private String imageUrl;

    /**
     * Overrides the asset's own alt text for this product. The localised alt lives on the asset; duplicating the
     * whole map here would be a second source of truth needing invalidation.
     */
    @Column(name = "ALT_TEXT")
    private String altText;

    @Column(name = "PRODUCT_IMAGE_URL")
    private String productImageUrl;

    @Column(name = "IMAGE_TYPE")
    private Integer imageType;

    @Column(name = "DEFAULT_IMAGE")
    private Boolean defaultImage;

    @Column(name = "SORT_ORDER")
    private Integer sortOrder;

    public ProductImage() {
    }

    public ProductImage(Product product, Long mediaAssetId, String imageUrl, String altText, int sortOrder,
                        boolean defaultImage) {
        this.product = product;
        this.mediaAssetId = mediaAssetId;
        this.imageUrl = imageUrl;
        this.altText = altText;
        this.sortOrder = sortOrder;
        this.defaultImage = defaultImage;
    }

    /**
     * Reads the type through {@link #getImageType()} rather than the raw column: an image created without one
     * set would otherwise throw on unboxing the first time it was read.
     */
    public boolean isExternal() {
        return getImageType() == TYPE_EXTERNAL_URL && productImageUrl != null;
    }

    /**
     * Where a browser fetches this image: the external url for an external row, else the cached library URL.
     */
    public String resolvedUrl() {
        return isExternal() ? productImageUrl : imageUrl;
    }

    public boolean isDefaultImage() {
        return defaultImage == null ? false : defaultImage;
    }

    public int getImageType() {
        return imageType == null ? TYPE_MEDIA_ASSET : imageType;
    }

    public int getSortOrder() {
        return sortOrder == null ? 0 : sortOrder;
    }
}
