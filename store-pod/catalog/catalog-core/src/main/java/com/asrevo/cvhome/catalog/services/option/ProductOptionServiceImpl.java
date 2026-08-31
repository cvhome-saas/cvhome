package com.asrevo.cvhome.catalog.services.option;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.catalog.entity.ProductOption;
import com.asrevo.cvhome.catalog.errors.DuplicateProductOptionException;
import com.asrevo.cvhome.catalog.errors.ProductOptionInUseException;
import com.asrevo.cvhome.catalog.errors.ProductOptionNotFoundException;
import com.asrevo.cvhome.catalog.model.option.PersistableProductOption;
import com.asrevo.cvhome.catalog.model.option.ReadableProductOption;
import com.asrevo.cvhome.catalog.repositories.ProductOptionRepository;
import com.asrevo.cvhome.catalog.services.Pages;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.model.entity.ReadableEntityList;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductOptionServiceImpl implements ProductOptionService {

    private final ProductOptionRepository productOptionRepository;

    @Override
    @Transactional(readOnly = true)
    public ReadableEntityList<ReadableProductOption> list(StoreMerchantId store, LanguageCode language,
                                                          Pageable pageable) {
        return Pages.toReadable(productOptionRepository.findByStoreMerchantId(store, pageable),
                o -> ProductOptionMapper.toReadable(o, language, true));
    }

    @Override
    @Transactional(readOnly = true)
    public ReadableProductOption get(StoreMerchantId store, Long id, LanguageCode language)
            throws ProductOptionNotFoundException {
        return ProductOptionMapper.toReadable(require(store, id), language, true);
    }

    @Override
    public boolean exists(StoreMerchantId store, String code) {
        return productOptionRepository.existsByStoreMerchantIdAndCode(store, code);
    }

    @Override
    @Transactional
    public Long create(StoreMerchantId store, PersistableProductOption source)
            throws DuplicateProductOptionException {
        if (exists(store, source.getCode())) {
            throw DuplicateProductOptionException.of(source.getCode(), store);
        }
        requireDistinctValueCodes(source);
        ProductOption option = new ProductOption();
        option.setStoreMerchantId(store);
        option.setCode(source.getCode());
        ProductOptionMapper.apply(source, option);
        return productOptionRepository.save(option).getId();
    }

    @Override
    @Transactional
    public void update(StoreMerchantId store, Long id, PersistableProductOption source)
            throws ProductOptionNotFoundException, DuplicateProductOptionException {
        requireDistinctValueCodes(source);
        ProductOptionMapper.apply(source, require(store, id));
    }

    @Override
    @Transactional
    public void delete(StoreMerchantId store, Long id)
            throws ProductOptionNotFoundException, ProductOptionInUseException {
        // Once products assign options (variants feature), deletion is additionally guarded by reference checks.
        productOptionRepository.delete(require(store, id));
    }

    private void requireDistinctValueCodes(PersistableProductOption source)
            throws DuplicateProductOptionException {
        if (ProductOptionMapper.hasDuplicateValueCodes(source)) {
            throw DuplicateProductOptionException.duplicateValue(source.getCode());
        }
    }

    private ProductOption require(StoreMerchantId store, Long id) throws ProductOptionNotFoundException {
        return productOptionRepository.findByStoreAndId(store, id)
                .orElseThrow(() -> ProductOptionNotFoundException.of(id, store));
    }
}
