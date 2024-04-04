package com.asrevo.cvhome.store.core.model.catalog.catalog;

import com.asrevo.cvhome.store.core.model.catalog.category.ReadableCategory;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

@Setter
@Getter
public class ReadableCatalogCategoryEntry extends CatalogEntryEntity {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;
    private String creationDate;
    /*	public ReadableProduct getProduct() {
            return product;
        }
        public void setProduct(ReadableProduct product) {
            this.product = product;
        }*/
    //private ReadableProduct product;
    private ReadableCategory category;

}
