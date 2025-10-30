package com.asrevo.cvhome.order.model.order;

import com.asrevo.cvhome.catalog.model.product.attribute.ProductAttribute;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PersistableOrderProduct extends OrderProductEntity implements Serializable {

	/**
	 *
	 */
	@Serial
	private static final long serialVersionUID = 1L;

	private BigDecimal price; // specify final price

	private List<ProductAttribute> attributes; // may have attributes

}
