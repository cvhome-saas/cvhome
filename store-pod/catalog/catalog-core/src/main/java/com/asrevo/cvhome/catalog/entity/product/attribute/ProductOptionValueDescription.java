package com.asrevo.cvhome.catalog.entity.product.attribute;

import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.common.description.Description;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.io.Serial;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "PRODUCT_OPTION_VALUE_DESCRIPTION",
		uniqueConstraints = { @UniqueConstraint(columnNames = { "PRODUCT_OPTION_VALUE_ID", "LANGUAGE_CODE" }) })
@TableGenerator(name = "description_gen", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
		valueColumnName = "SEQ_COUNT", pkColumnValue = "PRODUCT_OPTION_VALUE_DESCRIPTION_SEQ_NEXT_VAL",
		allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
		initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
@Getter
@Setter
public class ProductOptionValueDescription extends Description {

	@Serial
	private static final long serialVersionUID = 1L;

	@JsonIgnore
	@ManyToOne(targetEntity = ProductOptionValue.class)
	@JoinColumn(name = "PRODUCT_OPTION_VALUE_ID")
	private ProductOptionValue productOptionValue;

	public ProductOptionValueDescription() {
	}

}
