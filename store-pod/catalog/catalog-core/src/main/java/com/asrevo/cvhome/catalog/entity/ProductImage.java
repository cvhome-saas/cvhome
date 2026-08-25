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
 * One image of a product. {@code imageType} 0 is a file the store keeps on its CDN under the product's sku;
 * {@code imageType} 1 is an external url (possibly a video).
 */
@Entity
@Table(name = "PRODUCT_IMAGE")
@Getter
@Setter
public class ProductImage extends SalesManagerEntity<Long, ProductImage> {

    public static final int TYPE_FILE = 0;

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
     * The file name on the CDN.
     */
    @Column(name = "PRODUCT_IMAGE")
    private String productImage;

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

    public ProductImage(Product product, String fileName, int sortOrder, boolean defaultImage) {
        this.product = product;
        this.productImage = fileName;
        this.sortOrder = sortOrder;
        this.defaultImage = defaultImage;
    }

    /**
     * Reads the type through {@link #getImageType()} rather than the raw column: an image the upload path created
     * has never had one set, and unboxing that null threw on the first read of every freshly uploaded image.
     */
    public boolean isExternal() {
        return getImageType() == TYPE_EXTERNAL_URL && productImageUrl != null;
    }

    public boolean isDefaultImage() {
        return defaultImage == null ? false : defaultImage;
    }

    public int getImageType() {
        return imageType == null ? TYPE_FILE : imageType;
    }

    public int getSortOrder() {
        return sortOrder == null ? 0 : sortOrder;
    }
}
