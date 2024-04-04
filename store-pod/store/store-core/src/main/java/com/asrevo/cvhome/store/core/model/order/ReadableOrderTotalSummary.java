package com.asrevo.cvhome.store.core.model.order;


import com.asrevo.cvhome.store.core.model.order.total.ReadableOrderTotal;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class ReadableOrderTotalSummary implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String subTotal;//one time price for items
	private String total;//final price
	private String taxTotal;//total of taxes
	
	private List<ReadableOrderTotal> totals = new ArrayList<ReadableOrderTotal>();//all other fees (tax, shipping ....)

}
