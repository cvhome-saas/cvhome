package com.asrevo.cvhome.store.core.model.catalog.catalog;

import com.asrevo.cvhome.store.core.model.catalog.category.ReadableCategory;
import com.asrevo.cvhome.store.core.model.store.ReadableMerchantStore;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class ReadableCatalog extends ReadableCatalogName {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private ReadableMerchantStore store;

    private List<ReadableCategory> category = new ArrayList<ReadableCategory>();

	
/*	public List<ReadableCatalogCategoryEntry> getEntry() {
		return entry;
	}
	public void setEntry(List<ReadableCatalogCategoryEntry> entry) {
		this.entry = entry;
	}*/


}
