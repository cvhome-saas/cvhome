package com.asrevo.cvhome.catalog.entity.product.variant;

import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import jakarta.persistence.*;
import java.io.Serial;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "PRODUCT_VAR_IMAGE")
@Getter
@Setter
public class ProductVariantImage extends SalesManagerEntity<Long, ProductVariantImage> {

	@Serial
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "PRODUCT_VAR_IMAGE_ID")
	@TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
			valueColumnName = "SEQ_COUNT", pkColumnValue = "PRODUCT_VARIANT_IMAGE_SEQ_NEXT_VAL",
			allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
			initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
	private Long id;

	@Column(name = "PRODUCT_IMAGE")
	private String productImage;

	@Column(name = "DEFAULT_IMAGE")
	private boolean defaultImage = true;

	@ManyToOne(targetEntity = ProductVariantGroup.class)
	@JoinColumn(name = "PRODUCT_VARIANT_GROUP_ID", nullable = false)
	private ProductVariantGroup productVariantGroup;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "productVariantImage", cascade = CascadeType.ALL)
	private Set<ProductVariantImageDescription> descriptions = new HashSet<>();

	public ProductVariantImage() {
	}

}
