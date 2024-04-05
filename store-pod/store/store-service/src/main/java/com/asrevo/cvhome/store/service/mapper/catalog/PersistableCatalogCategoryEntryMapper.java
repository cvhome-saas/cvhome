package com.asrevo.cvhome.store.service.mapper.catalog;

import com.asrevo.cvhome.store.controller.exception.ConversionRuntimeException;
import com.asrevo.cvhome.store.core.entity.catalog.catalog.Catalog;
import com.asrevo.cvhome.store.core.entity.catalog.catalog.CatalogCategoryEntry;
import com.asrevo.cvhome.store.core.entity.catalog.category.Category;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.catalog.catalog.PersistableCatalogCategoryEntry;
import com.asrevo.cvhome.store.service.facade.catalog.CatalogFacade;
import com.asrevo.cvhome.store.service.facade.category.CategoryFacade;
import com.asrevo.cvhome.store.service.mapper.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class PersistableCatalogCategoryEntryMapper implements Mapper<PersistableCatalogCategoryEntry, CatalogCategoryEntry> {


    @Autowired
    private CategoryFacade categoryFacade;

    @Autowired
    @Lazy
    private CatalogFacade catalogFacade;


    @Override
    public CatalogCategoryEntry convert(PersistableCatalogCategoryEntry source, MerchantStore store, Language language) {
        CatalogCategoryEntry destination = new CatalogCategoryEntry();
        return this.merge(source, destination, store, language);
    }

    @Override
    public CatalogCategoryEntry merge(PersistableCatalogCategoryEntry source, CatalogCategoryEntry destination, MerchantStore store,
                                      Language language) {
        Assert.notNull(source, "CatalogEntry must not be null");
        Assert.notNull(store, "MerchantStore must not be null");
        Assert.notNull(source.getProductCode(), "ProductCode must not be null");
        Assert.notNull(source.getCategoryCode(), "CategoryCode must not be null");
        Assert.notNull(source.getCatalog(), "Catalog must not be null");


        if (destination == null) {
            destination = new CatalogCategoryEntry();

        }
        destination.setId(source.getId());
        destination.setVisible(source.isVisible());


        try {

            String catalog = source.getCatalog();

            Catalog catalogModel = catalogFacade.getCatalog(catalog, store);
            if (catalogModel == null) {
                throw new ConversionRuntimeException("Error while converting CatalogEntry product [" + source.getCatalog() + "] not found");
            }

            destination.setCatalog(catalogModel);

/*			Product productModel = productFacade.getProduct(source.getProductCode(), store);
			if(productModel == null) {
				throw new ConversionRuntimeException("Error while converting CatalogEntry product [" + source.getProductCode() + "] not found");
			}*/

            //destination.setProduct(productModel);

            Category categoryModel = categoryFacade.getByCode(source.getCategoryCode(), store);
            if (categoryModel == null) {
                throw new ConversionRuntimeException("Error while converting CatalogEntry category [" + source.getCategoryCode() + "] not found");
            }

            destination.setCategory(categoryModel);

        } catch (Exception e) {
            throw new ConversionRuntimeException("Error while converting CatalogEntry", e);
        }

        return destination;
    }

}
