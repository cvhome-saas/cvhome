package com.asrevo.cvhome.catalog.service.mapper.catalog;

import com.asrevo.cvhome.catalog.entity.product.attribute.ProductOption;
import com.asrevo.cvhome.catalog.entity.product.attribute.ProductOptionSet;
import com.asrevo.cvhome.catalog.entity.product.attribute.ProductOptionValue;
import com.asrevo.cvhome.catalog.entity.product.type.ProductType;
import com.asrevo.cvhome.catalog.model.product.attribute.optionset.PersistableProductOptionSet;
import com.asrevo.cvhome.catalog.services.product.attribute.ProductOptionService;
import com.asrevo.cvhome.catalog.services.product.attribute.ProductOptionValueService;
import com.asrevo.cvhome.catalog.services.product.type.ProductTypeService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.mapper.Mapper;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class PersistableProductOptionSetMapper
        implements Mapper<PersistableProductOptionSet, ProductOptionSet> {

    private final ProductOptionService productOptionService;

    private final ProductOptionValueService productOptionValueService;

    private final ProductTypeService productTypeService;

    public PersistableProductOptionSetMapper(
            ProductOptionService productOptionService,
            ProductOptionValueService productOptionValueService,
            ProductTypeService productTypeService) {
        this.productOptionService = productOptionService;
        this.productOptionValueService = productOptionValueService;
        this.productTypeService = productTypeService;
    }

    @Override
    public ProductOptionSet convert(
            PersistableProductOptionSet source, StoreMerchantId store, LanguageCode language) {

        ProductOptionSet optionSet = new ProductOptionSet();
        return this.merge(source, optionSet, store, language);
    }

    private ProductOptionValue value(Long productOptionValue, StoreMerchantId store) {
        return productOptionValueService.getById(store, productOptionValue);
    }

    @Override
    public ProductOptionSet merge(
            PersistableProductOptionSet source,
            ProductOptionSet destination,
            StoreMerchantId store,
            LanguageCode language) {
        Assert.notNull(destination, "ProductOptionSet must not be null");

        destination.setId(source.getId());
        destination.setCode(source.getCode());
        destination.setOptionDisplayOnly(source.isReadOnly());

        ProductOption option = productOptionService.getById(store, source.getOption());
        destination.setOption(option);

        if (!CollectionUtils.isEmpty(source.getOptionValues())) {
            List<ProductOptionValue> values =
                    source.getOptionValues().stream()
                            .map(id -> value(id, store))
                            .collect(Collectors.toList());
            destination.setValues(values);
        }

        if (!CollectionUtils.isEmpty(source.getProductTypes())) {
            List<ProductType> types =
                    productTypeService.listProductTypes(source.getProductTypes(), store, language);
            Set<ProductType> typesSet = new HashSet<>(types);
            destination.setProductTypes(typesSet);
        }

        return destination;
    }
}
