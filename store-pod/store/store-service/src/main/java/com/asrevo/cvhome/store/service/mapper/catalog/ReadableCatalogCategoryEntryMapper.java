package com.asrevo.cvhome.store.service.mapper.catalog;

import com.asrevo.cvhome.store.controller.exception.ConversionRuntimeException;
import com.asrevo.cvhome.store.core.entity.catalog.catalog.CatalogCategoryEntry;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.catalog.catalog.ReadableCatalogCategoryEntry;
import com.asrevo.cvhome.store.core.model.catalog.category.ReadableCategory;
import com.asrevo.cvhome.store.service.mapper.Mapper;
import com.asrevo.cvhome.store.utils.ImageFilePath;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ReadableCatalogCategoryEntryMapper implements Mapper<CatalogCategoryEntry, ReadableCatalogCategoryEntry> {


    private final ReadableCategoryMapper readableCategoryMapper;

    public ReadableCatalogCategoryEntryMapper(ReadableCategoryMapper readableCategoryMapper, ImageFilePath imageUtils) {
        this.readableCategoryMapper = readableCategoryMapper;
    }

    @Override
    public ReadableCatalogCategoryEntry convert(CatalogCategoryEntry source, MerchantStore store, Language language) {
        ReadableCatalogCategoryEntry destination = new ReadableCatalogCategoryEntry();
        return merge(source, destination, store, language);
    }

    @Override
    public ReadableCatalogCategoryEntry merge(CatalogCategoryEntry source, ReadableCatalogCategoryEntry destination, MerchantStore store,
                                              Language language) {
        ReadableCatalogCategoryEntry convertedDestination = Optional.ofNullable(destination)
                .orElse(new ReadableCatalogCategoryEntry());

        try {
            //ReadableProductPopulator readableProductPopulator = new ReadableProductPopulator();
            //readableProductPopulator.setImageUtils(imageUtils);
            //readableProductPopulator.setPricingService(pricingService);

            //ReadableProduct readableProduct = readableProductPopulator.populate(source.getProduct(), store, language);
            ReadableCategory readableCategory = readableCategoryMapper.convert(source.getCategory(), store, language);

            convertedDestination.setCatalog(source.getCatalog().getCode());

            convertedDestination.setId(source.getId());
            convertedDestination.setVisible(source.isVisible());
            convertedDestination.setCategory(readableCategory);
            //destination.setProduct(readableProduct);
            return convertedDestination;
        } catch (Exception e) {
            throw new ConversionRuntimeException("Error while creating ReadableCatalogEntry", e);
        }
    }
}
