package com.asrevo.cvhome.catalog.model.product.group;

import com.asrevo.cvhome.catalog.model.product.ReadableProduct;
import com.asrevo.cvhome.catalog.model.product.ReadableProductList;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReadableProductGroup extends ProductGroup implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	private ReadableProductGroupDescription description;

	private List<ReadableProductGroupDescription> descriptions = new ArrayList<>();

	private ReadableProduct parentProduct;

	private List<ReadableProduct> products = new ArrayList<>();

}
