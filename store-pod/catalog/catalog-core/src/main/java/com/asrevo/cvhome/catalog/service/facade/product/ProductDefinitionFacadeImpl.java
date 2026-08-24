package com.asrevo.cvhome.catalog.service.facade.product;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.errors.CategoryReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.errors.ManufacturerReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.errors.ProductAttributeNotConvertibleException;
import com.asrevo.cvhome.catalog.errors.ProductNotConvertibleException;
import com.asrevo.cvhome.catalog.errors.ProductNotFoundException;
import com.asrevo.cvhome.catalog.errors.ProductNotPersistedException;
import com.asrevo.cvhome.catalog.errors.ProductOptionNotConvertibleException;
import com.asrevo.cvhome.catalog.errors.ProductOptionReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.errors.ProductOptionValueReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.errors.ProductReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.errors.ProductTypeReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.model.product.product.definition.PersistableProductDefinition;
import com.asrevo.cvhome.catalog.model.product.product.definition.ReadableProductDefinition;
import com.asrevo.cvhome.catalog.service.mapper.catalog.product.PersistableProductDefinitionMapper;
import com.asrevo.cvhome.catalog.service.mapper.catalog.product.ReadableProductDefinitionMapper;
import com.asrevo.cvhome.catalog.services.product.ProductService;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

@Service("productDefinitionFacade")
// @Profile({"default", "cloud", "gcp", "aws", "mysql", "local"})
public class ProductDefinitionFacadeImpl implements ProductDefinitionFacade {

    private final ProductService productService;

    private final PersistableProductDefinitionMapper persistableProductDefinitionMapper;

    private final ReadableProductDefinitionMapper readableProductDefinitionMapper;

    public ProductDefinitionFacadeImpl(ProductService productService,
                                       PersistableProductDefinitionMapper persistableProductDefinitionMapper,
                                       ReadableProductDefinitionMapper readableProductDefinitionMapper) {
        this.productService = productService;
        this.persistableProductDefinitionMapper = persistableProductDefinitionMapper;
        this.readableProductDefinitionMapper = readableProductDefinitionMapper;
    }

    @Override
    public Long saveProductDefinition(StoreMerchantId store, PersistableProductDefinition product,
                                      LanguageCode language)
            throws ProductNotConvertibleException, ManufacturerReferenceUnresolvableException,
            ProductTypeReferenceUnresolvableException, CategoryReferenceUnresolvableException,
            ProductOptionReferenceUnresolvableException, ProductOptionValueReferenceUnresolvableException,
            ProductReferenceUnresolvableException, ProductAttributeNotConvertibleException,
            ProductOptionNotConvertibleException, ProductNotPersistedException,
            ProductNotFoundException {

        Product target;
        if (product.getId() != null && product.getId() > 0) {
            Optional<Product> p = productService.retrieveById(product.getId(), store);
            if (p.isEmpty()) {
                throw ProductNotFoundException.of(product.getId(), store);
            }
            target = p.get();
        } else {
            target = new Product();
        }

        target = persistableProductDefinitionMapper.merge(product, target, store, language);

        productService.saveProduct(target);
        product.setId(target.getId());

        return target.getId();
    }

    @Override
    public void update(Long id, PersistableProductDefinition product, StoreMerchantId merchant, LanguageCode language)
            throws ProductNotConvertibleException, ManufacturerReferenceUnresolvableException,
            ProductTypeReferenceUnresolvableException, CategoryReferenceUnresolvableException,
            ProductOptionReferenceUnresolvableException, ProductOptionValueReferenceUnresolvableException,
            ProductReferenceUnresolvableException, ProductAttributeNotConvertibleException,
            ProductOptionNotConvertibleException, ProductNotPersistedException,
            ProductNotFoundException {
        product.setId(id);
        this.saveProductDefinition(merchant, product, language);
    }

    @Override
    public ReadableProductDefinition getProduct(StoreMerchantId store, Long id, LanguageCode language) {
        Product product = productService.findOne(id, store);
        return readableProductDefinitionMapper.convert(product, store, language);
    }

}
