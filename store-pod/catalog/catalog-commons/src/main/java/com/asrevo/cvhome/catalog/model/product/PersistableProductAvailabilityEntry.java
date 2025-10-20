package com.asrevo.cvhome.catalog.model.product;

import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PersistableProductAvailabilityEntry implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	private String sku;

	private int quantity;

}
