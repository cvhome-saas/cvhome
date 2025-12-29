package com.asrevo.cvhome.checkout.model.order.v0;

import com.asrevo.cvhome.commons.domain.ReadableList;
import java.io.Serial;
import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Deprecated
public class ReadableOrderList extends ReadableList<ReadableOrder> implements Serializable {

	/**
	 *
	 */
	@Serial
	private static final long serialVersionUID = 1L;

}
