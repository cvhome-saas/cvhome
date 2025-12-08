package com.asrevo.cvhome.checkout.model.order.history;

import java.io.Serial;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReadableOrderStatusHistory extends OrderStatusHistory {

	/**
	 *
	 */
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * YYYY-mm-DD:HH mm SSS
	 */
	private String date;

}
