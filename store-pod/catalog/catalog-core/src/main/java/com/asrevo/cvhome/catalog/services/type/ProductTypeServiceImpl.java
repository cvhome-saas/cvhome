package com.asrevo.cvhome.catalog.services.type;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.catalog.entity.ProductType;
import com.asrevo.cvhome.catalog.errors.DuplicateProductTypeException;
import com.asrevo.cvhome.catalog.errors.ProductTypeNotFoundException;
import com.asrevo.cvhome.catalog.model.type.PersistableProductType;
import com.asrevo.cvhome.catalog.model.type.ReadableProductType;
import com.asrevo.cvhome.catalog.repositories.ProductTypeRepository;
import com.asrevo.cvhome.catalog.services.Pages;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.model.entity.ReadableEntityList;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductTypeServiceImpl implements ProductTypeService {

    private final ProductTypeRepository productTypeRepository;

    @Override
    @Transactional(readOnly = true)
    public ReadableEntityList<ReadableProductType> list(StoreMerchantId store, LanguageCode language,
                                                        Pageable pageable) {
        return Pages.toReadable(productTypeRepository.findByStoreMerchantId(store, pageable),
                t -> ProductTypeMapper.toReadable(t, language, true));
    }

    @Override
    @Transactional(readOnly = true)
    public ReadableProductType get(StoreMerchantId store, Long id, LanguageCode language)
            throws ProductTypeNotFoundException {
        return ProductTypeMapper.toReadable(require(store, id), language, true);
    }

    @Override
    public boolean exists(StoreMerchantId store, String code) {
        return productTypeRepository.existsByStoreMerchantIdAndCode(store, code);
    }

    @Override
    @Transactional
    public Long create(StoreMerchantId store, PersistableProductType source) throws DuplicateProductTypeException {
        if (exists(store, source.getCode())) {
            throw DuplicateProductTypeException.of(source.getCode(), store);
        }
        ProductType type = new ProductType();
        type.setStoreMerchantId(store);
        type.setCode(source.getCode());
        ProductTypeMapper.apply(source, type);
        return productTypeRepository.save(type).getId();
    }

    @Override
    @Transactional
    public void update(StoreMerchantId store, Long id, PersistableProductType source)
            throws ProductTypeNotFoundException {
        ProductTypeMapper.apply(source, require(store, id));
    }

    @Override
    @Transactional
    public void delete(StoreMerchantId store, Long id) throws ProductTypeNotFoundException {
        productTypeRepository.delete(require(store, id));
    }

    private ProductType require(StoreMerchantId store, Long id) throws ProductTypeNotFoundException {
        return productTypeRepository.findByStoreAndId(store, id)
                .orElseThrow(() -> ProductTypeNotFoundException.of(id, store));
    }
}
