package com.asrevo.cvhome.store.core.model.order;

import com.asrevo.cvhome.store.core.model.entity.Entity;

import java.io.Serializable;


public class OrderProduct extends Entity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String sku;
	public String getSku() {
		return sku;
	}
	public void setSku(String sku) {
		this.sku = sku;
	}

}
