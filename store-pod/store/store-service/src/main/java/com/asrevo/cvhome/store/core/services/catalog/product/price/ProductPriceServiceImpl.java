package com.asrevo.cvhome.store.core.services.catalog.product.price;


import com.asrevo.cvhome.store.core.entity.catalog.product.price.ProductPrice;
import com.asrevo.cvhome.store.core.entity.catalog.product.price.ProductPriceDescription;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.repositories.catalog.product.price.ProductPriceRepository;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("productPrice")
public class ProductPriceServiceImpl extends SalesManagerEntityServiceImpl<Long, ProductPrice>
        implements ProductPriceService {

    private final ProductPriceRepository productPriceRepository;

    @Autowired
    public ProductPriceServiceImpl(ProductPriceRepository productPriceRepository) {
        super(productPriceRepository);
        this.productPriceRepository = productPriceRepository;
    }

    @Override
    public void addDescription(ProductPrice price,
                               ProductPriceDescription description) throws ServiceException {
        price.getDescriptions().add(description);
        update(price);
    }


    @Override
    public ProductPrice saveOrUpdate(ProductPrice price) throws ServiceException {


        ProductPrice returnEntity = productPriceRepository.save(price);

        return returnEntity;


    }

    @Override
    public void delete(ProductPrice price) throws ServiceException {

        //override method, this allows the error that we try to remove a detached variant
        price = this.getById(price.getId());
        super.delete(price);

    }

    @Override
    public List<ProductPrice> findByProductSku(String sku, MerchantStore store) {

        return productPriceRepository.findByProduct(sku, store.getCode());
    }

    @Override
    public ProductPrice findById(Long priceId, String sku, MerchantStore store) {

        return productPriceRepository.findByProduct(sku, priceId, store.getCode());
    }

    @Override
    public List<ProductPrice> findByInventoryId(Long productInventoryId, String sku, MerchantStore store) {

        return productPriceRepository.findByProductInventoty(sku, productInventoryId, store.getCode());
    }


}
