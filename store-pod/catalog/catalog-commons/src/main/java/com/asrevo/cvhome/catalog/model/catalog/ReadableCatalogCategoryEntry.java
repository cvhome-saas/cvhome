package com.asrevo.cvhome.catalog.model.catalog;

import com.asrevo.cvhome.catalog.model.category.ReadableCategory;
import java.io.Serial;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReadableCatalogCategoryEntry extends CatalogEntryEntity {

	/**
	 *
	 */
	@Serial
	private static final long serialVersionUID = 1L;

	private String creationDate;

	/*
	 * public ReadableProduct getProduct() { return product; } public void
	 * setProduct(ReadableProduct product) { this.product = product; }
	 */
	// private ReadableProduct product;
	private ReadableCategory category;

}
