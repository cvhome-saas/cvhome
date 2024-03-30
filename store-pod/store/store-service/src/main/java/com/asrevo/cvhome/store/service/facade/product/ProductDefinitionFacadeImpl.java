package com.asrevo.cvhome.store.service.facade.product;

import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.services.catalog.product.ProductService;
import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.service.mapper.catalog.product.PersistableProductDefinitionMapper;
import com.asrevo.cvhome.store.service.mapper.catalog.product.ReadableProductDefinitionMapper;
import com.asrevo.cvhome.store.core.model.catalog.product.product.definition.PersistableProductDefinition;
import com.asrevo.cvhome.store.core.model.catalog.product.product.definition.ReadableProductDefinition;
import com.asrevo.cvhome.store.controller.exception.ResourceNotFoundException;
import com.asrevo.cvhome.store.controller.exception.ServiceRuntimeException;
import com.asrevo.cvhome.store.utils.ImageFilePath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service("productDefinitionFacade")
//@Profile({"default", "cloud", "gcp", "aws", "mysql", "local"})
public class ProductDefinitionFacadeImpl implements ProductDefinitionFacade {


    @Autowired
    private ProductService productService;


    @Autowired
    private PersistableProductDefinitionMapper persistableProductDefinitionMapper;

    @Autowired
    private ReadableProductDefinitionMapper readableProductDefinitionMapper;

    @Autowired
    private ProductVariantFacade productVariantFacade;

    @Autowired
    @Qualifier("img")
    private ImageFilePath imageUtils;

    @Override
    public Long saveProductDefinition(MerchantStore store, PersistableProductDefinition product, Language language) {


        Product target = null;
        if (product.getId() != null && product.getId().longValue() > 0) {
            Optional<Product> p = productService.retrieveById(product.getId(), store);
            if (p.isEmpty()) {
                throw new ResourceNotFoundException("Product with id [" + product.getId() + "] not found for store [" + store.getCode() + "]");
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
    public void update(Long id, PersistableProductDefinition product, MerchantStore merchant,
                       Language language) {
        product.setId(id);
        this.saveProductDefinition(merchant, product, language);
    }

    @Override
    public ReadableProductDefinition getProduct(MerchantStore store, Long id, Language language) {
        Product product = productService.findOne(id, store);
        return readableProductDefinitionMapper.convert(product, store, language);
    }

    @Override
    public ReadableProductDefinition getProductBySku(MerchantStore store, String uniqueCode, Language language) {

        Product product = null;
        try {
            product = productService.getBySku(uniqueCode, store, language);
        } catch (ServiceException e) {
            throw new ServiceRuntimeException(e);
        }
        return readableProductDefinitionMapper.convert(product, store, language);

    }

}
