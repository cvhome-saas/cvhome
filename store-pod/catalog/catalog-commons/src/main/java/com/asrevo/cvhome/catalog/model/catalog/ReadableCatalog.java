package com.asrevo.cvhome.catalog.model.catalog;

import com.asrevo.cvhome.catalog.model.category.ReadableCategory;
import com.asrevo.cvhome.merchant.model.merchant.ReadableMerchantStore;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReadableCatalog extends ReadableCatalogName {

	/**
	 *
	 */
	@Serial
	private static final long serialVersionUID = 1L;

	private ReadableMerchantStore store;

	private List<ReadableCategory> category = new ArrayList<>();

	/*
	 * public List<ReadableCatalogCategoryEntry> getEntry() { return entry; } public void
	 * setEntry(List<ReadableCatalogCategoryEntry> entry) { this.entry = entry; }
	 */

}
