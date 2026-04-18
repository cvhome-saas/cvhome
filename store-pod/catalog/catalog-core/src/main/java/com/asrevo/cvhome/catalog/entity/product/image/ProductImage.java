package com.asrevo.cvhome.catalog.entity.product.image;

import java.io.InputStream;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import jakarta.persistence.Transient;

import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "PRODUCT_IMAGE")
@Getter
@Setter
public class ProductImage extends SalesManagerEntity<Long, ProductImage> {

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

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "productImage", cascade = CascadeType.ALL)
    private List<ProductImageDescription> descriptions = new ArrayList<>();

    @Column(name = "PRODUCT_IMAGE")
    private String productImage;

    @Column(name = "DEFAULT_IMAGE")
    private boolean defaultImage = true;

    /**
     * default to 0 for images managed by the system
     */
    @Column(name = "IMAGE_TYPE")
    private int imageType;

    /**
     * Refers to images not accessible through the system. It may also be a video.
     */
    @Column(name = "PRODUCT_IMAGE_URL")
    private String productImageUrl;

    @Column(name = "IMAGE_CROP")
    private boolean imageCrop;

    @ManyToOne(targetEntity = Product.class)
    @JoinColumn(name = "PRODUCT_ID", nullable = false)
    private Product product;

    @Column(name = "SORT_ORDER")
    private Integer sortOrder = 0;

    @Transient
    private InputStream image = null;

}
