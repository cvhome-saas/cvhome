package com.asrevo.cvhome.catalog.services.manufacturer;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.catalog.entity.Category;
import com.asrevo.cvhome.catalog.entity.Manufacturer;
import com.asrevo.cvhome.catalog.errors.CategoryNotFoundException;
import com.asrevo.cvhome.catalog.errors.ManufacturerNotFoundException;
import com.asrevo.cvhome.catalog.model.manufacturer.PersistableManufacturer;
import com.asrevo.cvhome.catalog.model.manufacturer.ReadableManufacturer;
import com.asrevo.cvhome.catalog.repositories.CategoryRepository;
import com.asrevo.cvhome.catalog.repositories.ManufacturerRepository;
import com.asrevo.cvhome.catalog.services.Pages;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.model.entity.ReadableEntityList;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ManufacturerServiceImpl implements ManufacturerService {

    private final ManufacturerRepository manufacturerRepository;

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public ReadableEntityList<ReadableManufacturer> list(StoreMerchantId store, String name, LanguageCode language,
                                                         Pageable pageable) {
        Page<Manufacturer> page = name == null || name.isBlank() ? manufacturerRepository.findByStore(store, pageable)
                : manufacturerRepository.findByStoreAndName(store, name.trim(), pageable);
        return Pages.toReadable(page, m -> ManufacturerMapper.toReadable(m, language, true));
    }

    @Override
    @Transactional(readOnly = true)
    public ReadableManufacturer get(StoreMerchantId store, Long id, LanguageCode language)
            throws ManufacturerNotFoundException {
        return ManufacturerMapper.toReadable(require(store, id), language, true);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReadableManufacturer> listByCategory(StoreMerchantId store, Long categoryId, LanguageCode language)
            throws CategoryNotFoundException {
        Category category = categoryRepository.findByStoreAndId(store, categoryId)
                .orElseThrow(() -> CategoryNotFoundException.of(categoryId, store));
        return manufacturerRepository.findByCategorySubtree(store, category.subtreePrefix()).stream()
                .map(m -> ManufacturerMapper.toReadable(m, language, false))
                .toList();
    }

    @Override
    public boolean exists(StoreMerchantId store, String code) {
        return manufacturerRepository.existsByStoreMerchantIdAndCode(store, code);
    }

    @Override
    @Transactional
    public Long save(StoreMerchantId store, PersistableManufacturer source) throws ManufacturerNotFoundException {
        Manufacturer manufacturer;
        if (source.getId() == null || source.getId() <= 0) {
            manufacturer = new Manufacturer();
            manufacturer.setStoreMerchantId(store);
        } else {
            manufacturer = require(store, source.getId());
        }
        ManufacturerMapper.apply(source, manufacturer);
        return manufacturerRepository.save(manufacturer).getId();
    }

    @Override
    @Transactional
    public void delete(StoreMerchantId store, Long id) throws ManufacturerNotFoundException {
        manufacturerRepository.delete(require(store, id));
    }

    private Manufacturer require(StoreMerchantId store, Long id) throws ManufacturerNotFoundException {
        return manufacturerRepository.findByStoreAndId(store, id)
                .orElseThrow(() -> ManufacturerNotFoundException.of(id, store));
    }
}
