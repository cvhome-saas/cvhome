package com.asrevo.cvhome.catalog.services.product.variation;

import com.asrevo.cvhome.catalog.entity.product.variation.ProductVariation;
import com.asrevo.cvhome.catalog.repositories.product.variation.PageableProductVariationRepository;
import com.asrevo.cvhome.catalog.repositories.product.variation.ProductVariationRepository;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service("productVariationeService")
public class ProductVariationServiceImpl
        extends SalesManagerEntityServiceImpl<Long, ProductVariation>
        implements ProductVariationService {

    private final ProductVariationRepository productVariationRepository;
    private final PageableProductVariationRepository pageableProductVariationSetRepository;

    @Autowired
    public ProductVariationServiceImpl(
            ProductVariationRepository productVariationSetRepository,
            PageableProductVariationRepository pageableProductVariationSetRepository) {
        super(productVariationSetRepository);
        this.productVariationRepository = productVariationSetRepository;
        this.pageableProductVariationSetRepository = pageableProductVariationSetRepository;
    }

    @Override
    public Optional<ProductVariation> getById(StoreMerchantId store, Long id, LanguageCode lang) {
        return productVariationRepository.findOne(store, id, lang);
    }

    @Override
    public Optional<ProductVariation> getByCode(StoreMerchantId store, String code) {
        return productVariationRepository.findByCode(code, store);
    }

    @Override
    public Page<ProductVariation> getByMerchant(
            StoreMerchantId store, LanguageCode language, String code, int page, int count) {
        Pageable p = PageRequest.of(page, count);
        return pageableProductVariationSetRepository.list(store, code, p);
    }

    @Override
    public Optional<ProductVariation> getById(StoreMerchantId store, Long id) {
        return productVariationRepository.findOne(store, id);
    }

    @Override
    public void saveOrUpdate(ProductVariation entity) throws ServiceException {

        // save or update (persist and attach entities
        if (entity.getId() != null && entity.getId() > 0) {

            super.update(entity);

        } else {

            super.save(entity);
        }
    }

    @Override
    public List<ProductVariation> getByIds(List<Long> ids, StoreMerchantId store) {
        return productVariationRepository.findByIds(store, ids);
    }
}
