package com.asrevo.cvhome.store.service.mapper.catalog;

import com.asrevo.cvhome.store.controller.exception.ConversionRuntimeException;
import com.asrevo.cvhome.store.core.entity.catalog.product.attribute.ProductOption;
import com.asrevo.cvhome.store.core.entity.catalog.product.attribute.ProductOptionSet;
import com.asrevo.cvhome.store.core.entity.catalog.product.attribute.ProductOptionValue;
import com.asrevo.cvhome.store.core.entity.catalog.product.type.ProductType;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.catalog.product.attribute.optionset.PersistableProductOptionSet;
import com.asrevo.cvhome.store.core.services.catalog.product.attribute.ProductOptionService;
import com.asrevo.cvhome.store.core.services.catalog.product.attribute.ProductOptionValueService;
import com.asrevo.cvhome.store.core.services.catalog.product.type.ProductTypeService;
import com.asrevo.cvhome.store.service.mapper.Mapper;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class PersistableProductOptionSetMapper implements Mapper<PersistableProductOptionSet, ProductOptionSet> {

    private final ProductOptionService productOptionService;

    private final ProductOptionValueService productOptionValueService;

    private final ProductTypeService productTypeService;

    public PersistableProductOptionSetMapper(ProductOptionService productOptionService, ProductOptionValueService productOptionValueService, ProductTypeService productTypeService) {
        this.productOptionService = productOptionService;
        this.productOptionValueService = productOptionValueService;
        this.productTypeService = productTypeService;
    }

    @Override
    public ProductOptionSet convert(PersistableProductOptionSet source, MerchantStore store, Language language) {


        ProductOptionSet optionSet = new ProductOptionSet();
        return this.merge(source, optionSet, store, language);

    }

    private ProductOptionValue value(Long productOptionValue, MerchantStore store) {
        return productOptionValueService.getById(store, productOptionValue);
    }

    @Override
    public ProductOptionSet merge(PersistableProductOptionSet source, ProductOptionSet destination,
                                  MerchantStore store, Language language) {
        Assert.notNull(destination, "ProductOptionSet must not be null");

        destination.setId(source.getId());
        destination.setCode(source.getCode());
        destination.setOptionDisplayOnly(source.isReadOnly());

        ProductOption option = productOptionService.getById(store, source.getOption());
        destination.setOption(option);

        if (!CollectionUtils.isEmpty(source.getOptionValues())) {
            List<ProductOptionValue> values = source.getOptionValues().stream().map(id -> value(id, store)).collect(Collectors.toList());
            destination.setValues(values);
        }

        if (!CollectionUtils.isEmpty(source.getProductTypes())) {
            try {
                List<ProductType> types = productTypeService.listProductTypes(source.getProductTypes(), store, language);
                Set<ProductType> typesSet = new HashSet<ProductType>(types);
                destination.setProductTypes(typesSet);
            } catch (ServiceException e) {
                throw new ConversionRuntimeException("Error while mpping ProductOptionSet", e);
            }
        }

        return destination;
    }

}
