package com.asrevo.cvhome.store.core.services.catalog.product.variation;

import com.asrevo.cvhome.store.core.entity.catalog.product.variation.ProductVariation;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.repositories.catalog.product.variation.PageableProductVariationRepository;
import com.asrevo.cvhome.store.core.repositories.catalog.product.variation.ProductVariationRepository;
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
    public Optional<ProductVariation> getById(MerchantStore store, Long id, Language lang) {
        return productVariationRepository.findOne(store.getId(), id, lang.getId());
    }

    @Override
    public Optional<ProductVariation> getByCode(MerchantStore store, String code) {
        return productVariationRepository.findByCode(code, store.getId());
    }

    @Override
    public Page<ProductVariation> getByMerchant(
            MerchantStore store, Language language, String code, int page, int count) {
        Pageable p = PageRequest.of(page, count);
        return pageableProductVariationSetRepository.list(store.getId(), code, p);
    }

    @Override
    public Optional<ProductVariation> getById(MerchantStore store, Long id) {
        return productVariationRepository.findOne(store.getId(), id);
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
    public List<ProductVariation> getByIds(List<Long> ids, MerchantStore store) {
        return productVariationRepository.findByIds(store.getId(), ids);
    }
}
