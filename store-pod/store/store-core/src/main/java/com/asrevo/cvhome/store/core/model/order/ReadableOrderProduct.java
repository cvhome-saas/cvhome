package com.asrevo.cvhome.store.core.model.order;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Setter
@Getter
public class ReadableOrderProduct extends OrderProductEntity implements
		Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String productName;
	private String price;
	private String subTotal;
	
	private List<ReadableOrderProductAttribute> attributes = null;
	
	private String sku;
	private String image;


}
