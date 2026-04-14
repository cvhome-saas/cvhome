package com.asrevo.cvhome.catalog.model.product.group;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PersistableProductGroup extends ProductGroup implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	private List<PersistableProductGroupDescription> descriptions = new ArrayList<>();

	private Long parentProductId;

	private List<Long> productIds = new ArrayList<>();

}
