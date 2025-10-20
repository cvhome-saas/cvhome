package com.asrevo.cvhome.order.entity.order;

import com.asrevo.cvhome.store.core.entity.common.EntityList;
import java.io.Serial;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OrderList extends EntityList {

	/**
	 *
	 */
	@Serial
	private static final long serialVersionUID = -6645927228659963628L;

	private List<Order> orders;

}
