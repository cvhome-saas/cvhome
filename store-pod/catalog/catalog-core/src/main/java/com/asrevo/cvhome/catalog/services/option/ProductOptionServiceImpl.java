package com.asrevo.cvhome.catalog.services.option;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.catalog.entity.ProductOption;
import com.asrevo.cvhome.catalog.entity.ProductOptionValue;
import com.asrevo.cvhome.catalog.errors.DuplicateProductOptionException;
import com.asrevo.cvhome.catalog.errors.ProductOptionInUseException;
import com.asrevo.cvhome.catalog.errors.ProductOptionNotFoundException;
import com.asrevo.cvhome.catalog.model.option.PersistableProductOption;
import com.asrevo.cvhome.catalog.model.option.PersistableProductOptionValue;
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
            throws ProductOptionNotFoundException, DuplicateProductOptionException, ProductOptionInUseException {
        requireDistinctValueCodes(source);
        ProductOption option = require(store, id);
        requireDroppedValuesUnused(store, option, source);
        ProductOptionMapper.apply(source, option);
    }

    /**
     * A value the payload leaves out is orphan-removed, so dropping one a variant still sells by has to be
     * refused here — the delete path already refuses the same thing for the whole option, and without this the
     * merchant met a raw foreign-key 500 on the neighbouring endpoint instead of the named 409.
     */
    private void requireDroppedValuesUnused(StoreMerchantId store, ProductOption option,
                                            PersistableProductOption source)
            throws ProductOptionInUseException {
        Set<Long> kept = source.getValues().stream().map(PersistableProductOptionValue::getId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        List<Long> dropped = option.getValues().stream().map(ProductOptionValue::getId)
                .filter(valueId -> !kept.contains(valueId)).toList();
        if (dropped.isEmpty()) {
            return;
        }
        List<Long> stillSold = productOptionRepository.valueIdsUsedByVariants(dropped);
        if (!stillSold.isEmpty()) {
            throw ProductOptionInUseException.of(option.getId(), store);
        }
    }

    @Override
    @Transactional
    public void delete(StoreMerchantId store, Long id)
            throws ProductOptionNotFoundException, ProductOptionInUseException {
        ProductOption option = require(store, id);
        if (productOptionRepository.isAssignedToProducts(id) || productOptionRepository.isUsedByVariants(id)) {
            throw ProductOptionInUseException.of(option.getCode(), store);
        }
        productOptionRepository.delete(option);
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
