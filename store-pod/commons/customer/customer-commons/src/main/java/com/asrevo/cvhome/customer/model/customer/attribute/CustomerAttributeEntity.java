package com.asrevo.cvhome.customer.model.customer.attribute;

import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CustomerAttributeEntity extends CustomerAttribute implements Serializable {

	/**
	 *
	 */
	@Serial
	private static final long serialVersionUID = 1L;

	private String textValue;

}
