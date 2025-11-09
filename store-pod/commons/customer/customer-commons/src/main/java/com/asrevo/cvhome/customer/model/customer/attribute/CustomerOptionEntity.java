package com.asrevo.cvhome.customer.model.customer.attribute;

import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CustomerOptionEntity extends CustomerOption implements Serializable {

	/**
	 *
	 */
	@Serial
	private static final long serialVersionUID = 1L;

	private int order;

	private String code;

	private String type; // TEXT|SELECT|RADIO|CHECKBOX

}
