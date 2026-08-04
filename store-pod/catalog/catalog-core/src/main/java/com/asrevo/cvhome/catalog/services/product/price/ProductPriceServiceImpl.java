package com.asrevo.cvhome.catalog.services.product.price;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.asrevo.cvhome.catalog.entity.product.price.ProductPrice;
import com.asrevo.cvhome.catalog.repositories.product.price.ProductPriceRepository;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;

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
    public ProductPrice saveOrUpdate(ProductPrice price) {
        return productPriceRepository.save(price);
    }

    @Override
    public void delete(ProductPrice price) {
        // override method, this allows the error that we try to remove a detached variant
        price = this.getById(price.getId());
        super.delete(price);
    }

    @Override
    public List<ProductPrice> findByProductSku(String sku, StoreMerchantId store) {
        return productPriceRepository.findByProduct(sku, store);
    }

    @Override
    public ProductPrice findById(Long priceId, String sku, StoreMerchantId store) {
        return productPriceRepository.findByProduct(sku, priceId, store);
    }

    @Override
    public List<ProductPrice> findByInventoryId(Long productInventoryId, String sku, StoreMerchantId store) {
        return productPriceRepository.findByProductInventoty(sku, productInventoryId, store);
    }

}
