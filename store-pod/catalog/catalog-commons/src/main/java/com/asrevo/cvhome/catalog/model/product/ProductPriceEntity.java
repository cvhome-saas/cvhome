package com.asrevo.cvhome.catalog.model.product;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 * A product entity is used by services API to populate or retrieve a Product price entity
 *
 * @author Carl Samson
 */
@Setter
@Getter
public class ProductPriceEntity extends ProductPrice implements Serializable {

	/**
	 *
	 */
	@Serial
	private static final long serialVersionUID = 1L;

	private String code;

	private boolean discounted = false;

	private String discountStartDate;

	private String discountEndDate;

	private boolean defaultPrice = true;

	private BigDecimal price;

	private BigDecimal discountedPrice;

	public String getCode() {
		if (StringUtils.isBlank(this.code)) {
			code = DEFAULT_PRICE_CODE;
		}
		return code;
	}

}
