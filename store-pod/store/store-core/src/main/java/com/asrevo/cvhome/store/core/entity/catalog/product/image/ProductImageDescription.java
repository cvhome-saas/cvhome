package com.asrevo.cvhome.store.core.entity.catalog.product.image;

import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.common.description.Description;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

@Entity
@Table(name = "PRODUCT_IMAGE_DESCRIPTION", uniqueConstraints = {
        @UniqueConstraint(columnNames = {
                "PRODUCT_IMAGE_ID",
                "LANGUAGE_ID"
        })
}
)
@TableGenerator(name = "description_gen", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT", pkColumnValue = "product_image_description_seq", allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE, initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
@Getter
@Setter
public class ProductImageDescription extends Description {
    @Serial
    private static final long serialVersionUID = 1L;

    @ManyToOne(targetEntity = ProductImage.class)
    @JoinColumn(name = "PRODUCT_IMAGE_ID", nullable = false)
    private ProductImage productImage;

    @Column(name = "ALT_TAG", length = 100)
    private String altTag;
}
