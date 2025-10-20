package com.asrevo.cvhome.catalog.model.product.product;

import com.asrevo.cvhome.catalog.model.category.Category;
import com.asrevo.cvhome.catalog.model.product.PersistableImage;
import com.asrevo.cvhome.catalog.model.product.ProductDescription;
import com.asrevo.cvhome.catalog.model.product.attribute.PersistableProductAttribute;
import com.asrevo.cvhome.catalog.model.product.product.variant.PersistableProductVariant;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PersistableProduct extends ProductEntity implements Serializable {

	/**
	 *
	 */
	@Serial
	private static final long serialVersionUID = 1L;

	private List<ProductDescription> descriptions = new ArrayList<>();

	private List<PersistableProductAttribute> attributes = new ArrayList<>(); // persist
																				// attribute
																				// and
																				// save
																				// reference

	private List<PersistableImage> images; // persist images and save reference

	private List<Category> categories = new ArrayList<>();

	private PersistableProductInventory inventory;

	private List<PersistableProductVariant> variants = new ArrayList<>();

	private String type;

}
