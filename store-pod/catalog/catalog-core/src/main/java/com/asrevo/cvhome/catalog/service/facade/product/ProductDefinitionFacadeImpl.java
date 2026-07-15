package com.asrevo.cvhome.catalog.service.facade.product;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.model.product.product.definition.PersistableProductDefinition;
import com.asrevo.cvhome.catalog.model.product.product.definition.ReadableProductDefinition;
import com.asrevo.cvhome.catalog.service.mapper.catalog.product.PersistableProductDefinitionMapper;
import com.asrevo.cvhome.catalog.service.mapper.catalog.product.ReadableProductDefinitionMapper;
import com.asrevo.cvhome.catalog.services.product.ProductService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.controller.exception.ResourceNotFoundException;
import com.asrevo.cvhome.store.controller.exception.ServiceRuntimeException;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.store.utils.ImageFilePath;

@Service("productDefinitionFacade")
// @Profile({"default", "cloud", "gcp", "aws", "mysql", "local"})
public class ProductDefinitionFacadeImpl implements ProductDefinitionFacade {

    private final ProductService productService;

    private final PersistableProductDefinitionMapper persistableProductDefinitionMapper;

    private final ReadableProductDefinitionMapper readableProductDefinitionMapper;

    public ProductDefinitionFacadeImpl(ProductService productService,
                                       PersistableProductDefinitionMapper persistableProductDefinitionMapper,
                                       ReadableProductDefinitionMapper readableProductDefinitionMapper,
                                       ProductVariantFacade productVariantFacade,
                                       ImageFilePath imageUtils) {
        this.productService = productService;
        this.persistableProductDefinitionMapper = persistableProductDefinitionMapper;
        this.readableProductDefinitionMapper = readableProductDefinitionMapper;
    }

    @Override
    public Long saveProductDefinition(StoreMerchantId store, PersistableProductDefinition product,
                                      LanguageCode language) {

        Product target;
        if (product.getId() != null && product.getId() > 0) {
            Optional<Product> p = productService.retrieveById(product.getId(), store);
            if (p.isEmpty()) {
                throw new ResourceNotFoundException(
                        "Product with id [" + product.getId() + "] not found for store [" + store + "]");
            }
            target = p.get();
        } else {
            target = new Product();
        }

        try {
            target = persistableProductDefinitionMapper.merge(product, target, store, language);

            productService.saveProduct(target);
            product.setId(target.getId());

            return target.getId();
        } catch (Exception e) {
            throw new ServiceRuntimeException(e);
        }
    }

    @Override
    public void update(Long id, PersistableProductDefinition product, StoreMerchantId merchant, LanguageCode language) {
        product.setId(id);
        this.saveProductDefinition(merchant, product, language);
    }

    @Override
    public ReadableProductDefinition getProduct(StoreMerchantId store, Long id, LanguageCode language) {
        Product product = productService.findOne(id, store);
        return readableProductDefinitionMapper.convert(product, store, language);
    }

}
