package com.asrevo.cvhome.order.model.order.total;

import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReadableOrderTotal extends OrderTotal implements Serializable {

	/**
	 *
	 */
	@Serial
	private static final long serialVersionUID = 1L;

	private String total;

	private boolean discounted;

}
