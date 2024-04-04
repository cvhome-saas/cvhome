package com.asrevo.cvhome.store.core.model.order.total;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * Serves as the order total summary calculation
 * @author c.samson
 *
 */
@Setter
@Getter
public class ReadableTotal implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private List<ReadableOrderTotal> totals;
	private String grandTotal;

}
