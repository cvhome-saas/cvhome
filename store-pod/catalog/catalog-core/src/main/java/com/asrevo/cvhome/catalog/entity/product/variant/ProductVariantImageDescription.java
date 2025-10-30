package com.asrevo.cvhome.catalog.entity.product.variant;

import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.common.description.Description;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.io.Serial;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "PRODUCT_VAR_IMAGE_DESCRIPTION",
		uniqueConstraints = { @UniqueConstraint(columnNames = { "PRODUCT_VAR_IMAGE_ID", "LANGUAGE_CODE" }) })
@TableGenerator(name = "description_gen", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
		valueColumnName = "SEQ_COUNT", pkColumnValue = "PRODUCT_VARIANT_IMAGE_DESCRIPTION_SEQ_NEXT_VAL",
		allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
		initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
@Getter
@Setter
public class ProductVariantImageDescription extends Description {

	@Serial
	private static final long serialVersionUID = 1L;

	@ManyToOne(targetEntity = ProductVariantImage.class)
	@JoinColumn(name = "PRODUCT_VAR_IMAGE_ID", nullable = false)
	private ProductVariantImage productVariantImage;

	@JsonIgnore
	@ManyToOne(targetEntity = Product.class)
	@JoinColumn(name = "PRODUCT_ID", nullable = false)
	private Product product;

	@Column(name = "ALT_TAG", length = 100)
	private String altTag;

}
