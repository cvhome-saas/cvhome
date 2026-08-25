package com.asrevo.cvhome.catalog.services.group;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.catalog.entity.Product;
import com.asrevo.cvhome.catalog.entity.ProductGroup;
import com.asrevo.cvhome.catalog.errors.ProductGroupNotFoundException;
import com.asrevo.cvhome.catalog.errors.ProductNotFoundException;
import com.asrevo.cvhome.catalog.model.group.PersistableProductGroup;
import com.asrevo.cvhome.catalog.model.group.ReadableProductGroup;
import com.asrevo.cvhome.catalog.repositories.ProductGroupRepository;
import com.asrevo.cvhome.catalog.repositories.ProductRepository;
import com.asrevo.cvhome.catalog.services.Pages;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.model.entity.ReadableEntityList;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductGroupServiceImpl implements ProductGroupService {

    private final ProductGroupRepository productGroupRepository;

    private final ProductRepository productRepository;

    private final ProductGroupMapper productGroupMapper;

    @Override
    @Transactional(readOnly = true)
    public ReadableEntityList<ReadableProductGroup> list(StoreMerchantId store, LanguageCode language,
                                                         Pageable pageable) {
        return Pages.toReadable(productGroupRepository.findByStore(store, pageable),
                g -> productGroupMapper.summary(g, language, true));
    }

    @Override
    @Transactional(readOnly = true)
    public ReadableProductGroup get(StoreMerchantId store, String code, LanguageCode language, boolean allLanguages)
            throws ProductGroupNotFoundException {
        return productGroupMapper.toReadable(require(store, code), language, allLanguages);
    }

    @Override
    public boolean exists(StoreMerchantId store, String code) {
        return productGroupRepository.findByStoreAndCode(store, code).isPresent();
    }

    @Override
    @Transactional
    public PersistableProductGroup save(StoreMerchantId store, PersistableProductGroup source)
            throws ProductGroupNotFoundException, ProductNotFoundException {
        ProductGroup group = source.getId() == null || source.getId() <= 0
                ? productGroupRepository.findByStoreAndCode(store, source.getCode())
                        .orElseGet(() -> new ProductGroup(store, source.getCode(), null))
                : productGroupRepository.findById(source.getId())
                        .filter(g -> g.getStoreMerchantId().equals(store))
                        .orElseThrow(() -> ProductGroupNotFoundException.of(source.getId(), store));
        ProductGroupMapper.apply(source, group);
        group.setParentProduct(source.getParentProductId() == null ? null
                : requireProduct(store, source.getParentProductId()));
        List<Product> members = new ArrayList<>();
        for (Long productId : source.getProductIds()) {
            members.add(requireProduct(store, productId));
        }
        group.setProducts(members);
        source.setId(productGroupRepository.save(group).getId());
        return source;
    }

    @Override
    @Transactional
    public void delete(StoreMerchantId store, String code) throws ProductGroupNotFoundException {
        productGroupRepository.delete(require(store, code));
    }

    @Override
    @Transactional
    public void addProduct(StoreMerchantId store, String code, Long productId)
            throws ProductGroupNotFoundException, ProductNotFoundException {
        ProductGroup group = require(store, code);
        if (!group.contains(productId)) {
            group.getProducts().add(requireProduct(store, productId));
        }
    }

    @Override
    @Transactional
    public void removeProduct(StoreMerchantId store, String code, Long productId)
            throws ProductGroupNotFoundException {
        require(store, code).getProducts().removeIf(p -> p.getId().equals(productId));
    }

    @Override
    @Transactional(readOnly = true)
    public ReadableProductGroup related(StoreMerchantId store, Long productId, LanguageCode language)
            throws ProductGroupNotFoundException {
        ProductGroup group = productGroupRepository.findByStoreAndParentProductAndCode(store, productId, RELATED_ITEMS)
                .orElseThrow(() -> ProductGroupNotFoundException.of(RELATED_ITEMS, store));
        return productGroupMapper.toReadable(group, language, false);
    }

    @Override
    @Transactional
    public void addRelated(StoreMerchantId store, Long productId, Long relatedProductId)
            throws ProductNotFoundException {
        Product product = requireProduct(store, productId);
        ProductGroup group = productGroupRepository.findByStoreAndParentProductAndCode(store, productId, RELATED_ITEMS)
                .orElseGet(() -> new ProductGroup(store, RELATED_ITEMS, product));
        if (!group.contains(relatedProductId)) {
            group.getProducts().add(requireProduct(store, relatedProductId));
        }
        productGroupRepository.save(group);
    }

    @Override
    @Transactional
    public void removeRelated(StoreMerchantId store, Long productId, Long relatedProductId)
            throws ProductGroupNotFoundException {
        ProductGroup group = productGroupRepository.findByStoreAndParentProductAndCode(store, productId, RELATED_ITEMS)
                .orElseThrow(() -> ProductGroupNotFoundException.of(RELATED_ITEMS, store));
        group.getProducts().removeIf(p -> p.getId().equals(relatedProductId));
    }

    private ProductGroup require(StoreMerchantId store, String code) throws ProductGroupNotFoundException {
        return productGroupRepository.findByStoreAndCode(store, code)
                .orElseThrow(() -> ProductGroupNotFoundException.of(code, store));
    }

    private Product requireProduct(StoreMerchantId store, Long id) throws ProductNotFoundException {
        return productRepository.findByStoreAndId(store, id).orElseThrow(() -> ProductNotFoundException.of(id, store));
    }
}
