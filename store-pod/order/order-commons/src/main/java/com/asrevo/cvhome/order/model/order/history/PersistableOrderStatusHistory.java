package com.asrevo.cvhome.order.model.order.history;

import java.io.Serial;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PersistableOrderStatusHistory extends OrderStatusHistory {

	/**
	 *
	 */
	@Serial
	private static final long serialVersionUID = 1L;

	private String date;

}
