package com.asrevo.cvhome.catalog.services.product;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.billing.commons.EntitlementKey;
import com.asrevo.cvhome.billing.commons.errors.EntitlementExceededException;
import com.asrevo.cvhome.billing.guard.StoreEntitlements;
import com.asrevo.cvhome.catalog.entity.Category;
import com.asrevo.cvhome.catalog.entity.Product;
import com.asrevo.cvhome.catalog.errors.CategoryAlreadyAttachedException;
import com.asrevo.cvhome.catalog.errors.CategoryNotFoundException;
import com.asrevo.cvhome.catalog.errors.CategoryReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.errors.ManufacturerReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.errors.ProductNotFoundException;
import com.asrevo.cvhome.catalog.errors.ProductTypeReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.model.category.CategoryReference;
import com.asrevo.cvhome.catalog.model.product.LightPersistableProduct;
import com.asrevo.cvhome.catalog.model.product.PersistableProductDefinition;
import com.asrevo.cvhome.catalog.model.product.ProductFilter;
import com.asrevo.cvhome.catalog.model.product.ReadableMinimalProduct;
import com.asrevo.cvhome.catalog.model.product.ReadableProduct;
import com.asrevo.cvhome.catalog.model.product.ReadableProductDefinition;
import com.asrevo.cvhome.catalog.repositories.CategoryRepository;
import com.asrevo.cvhome.catalog.repositories.ManufacturerRepository;
import com.asrevo.cvhome.catalog.repositories.ProductRepository;
import com.asrevo.cvhome.catalog.repositories.ProductTypeRepository;
import com.asrevo.cvhome.catalog.services.Pages;
import com.asrevo.cvhome.catalog.services.image.ProductImageService;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.model.entity.ReadableEntityList;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    private final CategoryRepository categoryRepository;

    private final ManufacturerRepository manufacturerRepository;

    private final ProductTypeRepository productTypeRepository;

    private final ProductImageService productImageService;

    private final ProductMapper productMapper;

    private final StoreEntitlements storeEntitlements;

    @Override
    @Transactional(readOnly = true)
    public ReadableEntityList<ReadableProduct> list(StoreMerchantId store, ProductFilter filter,
                                                    LanguageCode language, Pageable pageable) {
        if (filter.getCategoryIds() != null && filter.getCategoryIds().size() == 1) {
            categoryRepository.findByStoreAndId(store, filter.getCategoryIds().getFirst())
                    .ifPresent(category -> filter.setCategoryIds(
                            categoryRepository.findSubtree(store, category.subtreePrefix()).stream()
                                    .map(Category::getId).toList()));
        }
        return Pages.toReadable(productRepository.search(store, filter, pageable),
                p -> productMapper.toReadable(p, language));
    }

    @Override
    @Transactional(readOnly = true)
    public ReadableProduct getByFriendlyUrl(StoreMerchantId store, String friendlyUrl, LanguageCode language)
            throws ProductNotFoundException {
        Product product = productRepository.findByStoreAndFriendlyUrl(store, friendlyUrl, language)
                .orElseThrow(() -> ProductNotFoundException.of(friendlyUrl, store));
        return productMapper.toReadable(product, language);
    }

    @Override
    @Transactional(readOnly = true)
    public ReadableMinimalProduct getBySku(StoreMerchantId store, String sku, LanguageCode language)
            throws ProductNotFoundException {
        Product product = productRepository.findByStoreAndSku(store, sku)
                .orElseThrow(() -> ProductNotFoundException.of(sku, store));
        return productMapper.toMinimal(product, language);
    }

    @Override
    @Transactional(readOnly = true)
    public ReadableProductDefinition getDefinition(StoreMerchantId store, Long id, LanguageCode language)
            throws ProductNotFoundException {
        return productMapper.toDefinition(require(store, id), language);
    }

    @Override
    public boolean exists(StoreMerchantId store, String sku) {
        return productRepository.existsByStoreAndSku(store, sku);
    }

    @Override
    @Transactional
    public Long create(StoreMerchantId store, PersistableProductDefinition source)
            throws ManufacturerReferenceUnresolvableException, ProductTypeReferenceUnresolvableException,
            CategoryReferenceUnresolvableException, EntitlementExceededException {
        // Only a new product can take the store past its plan's ceiling; the count runs only when a plan caps it.
        storeEntitlements.require(store, EntitlementKey.MAX_PRODUCTS, () -> productRepository.countByStore(store));
        Product product = new Product();
        product.setStore(store);
        applyDefinition(store, source, product);
        return productRepository.save(product.searchIndexStale()).getId();
    }

    @Override
    @Transactional
    public void update(StoreMerchantId store, Long id, PersistableProductDefinition source)
            throws ProductNotFoundException, ManufacturerReferenceUnresolvableException,
            ProductTypeReferenceUnresolvableException, CategoryReferenceUnresolvableException {
        Product product = require(store, id);
        applyDefinition(store, source, product);
        productRepository.save(product.searchIndexStale());
    }

    private void applyDefinition(StoreMerchantId store, PersistableProductDefinition source, Product product)
            throws ManufacturerReferenceUnresolvableException, ProductTypeReferenceUnresolvableException,
            CategoryReferenceUnresolvableException {
        ProductMapper.apply(source, product);
        product.setManufacturer(resolveManufacturer(store, source.getManufacturer()));
        product.setType(resolveType(store, source.getType()));
        if (!source.getCategories().isEmpty()) {
            Set<Category> categories = new HashSet<>();
            for (CategoryReference reference : source.getCategories()) {
                categories.add(resolveCategory(store, reference));
            }
            product.setCategories(categories);
        }
    }

    @Override
    @Transactional
    public void patch(StoreMerchantId store, Long id, LightPersistableProduct source)
            throws ProductNotFoundException {
        Product product = require(store, id);
        product.setAvailable(source.isAvailable());
        product.setProductShipeable(source.isProductShipeable());
        productRepository.save(product.searchIndexStale());
    }

    @Override
    @Transactional
    public void addToCategory(StoreMerchantId store, Long productId, Long categoryId)
            throws ProductNotFoundException, CategoryNotFoundException, CategoryAlreadyAttachedException {
        Product product = require(store, productId);
        Category category = requireCategory(store, categoryId);
        if (product.getCategories().contains(category)) {
            throw CategoryAlreadyAttachedException.of(categoryId, productId);
        }
        product.getCategories().add(category);
        productRepository.save(product.searchIndexStale());
    }

    @Override
    @Transactional
    public void removeFromCategory(StoreMerchantId store, Long productId, Long categoryId)
            throws ProductNotFoundException, CategoryNotFoundException {
        Product product = require(store, productId);
        product.getCategories().remove(requireCategory(store, categoryId));
        productRepository.save(product.searchIndexStale());
    }

    @Override
    @Transactional
    public void delete(StoreMerchantId store, Long id) throws ProductNotFoundException {
        delete(store, require(store, id));
    }

    @Override
    @Transactional
    public void delete(StoreMerchantId store, Product product) {
        // Releases the product's hold on its media assets. The assets themselves stay in the library — they may
        // be used by other products, and deleting someone's uploads because a product went away would be wrong.
        product.getImages().clear();
        productImageService.forget(product);
        productRepository.delete(product.searchIndexPurged());
    }

    private Product require(StoreMerchantId store, Long id) throws ProductNotFoundException {
        return productRepository.findByStoreAndId(store, id).orElseThrow(() -> ProductNotFoundException.of(id, store));
    }

    private Category requireCategory(StoreMerchantId store, Long id) throws CategoryNotFoundException {
        return categoryRepository.findByStoreAndId(store, id).orElseThrow(() -> CategoryNotFoundException.of(id, store));
    }

    private com.asrevo.cvhome.catalog.entity.Manufacturer resolveManufacturer(StoreMerchantId store, String code)
            throws ManufacturerReferenceUnresolvableException {
        if (code == null || code.isBlank()) {
            return null;
        }
        return manufacturerRepository.findByStoreAndCode(store, code)
                .orElseThrow(() -> ManufacturerReferenceUnresolvableException.of(code, store));
    }

    private com.asrevo.cvhome.catalog.entity.ProductType resolveType(StoreMerchantId store, String code)
            throws ProductTypeReferenceUnresolvableException {
        if (code == null || code.isBlank()) {
            return null;
        }
        return productTypeRepository.findByStoreAndCode(store, code)
                .orElseThrow(() -> ProductTypeReferenceUnresolvableException.of(code, store));
    }

    private Category resolveCategory(StoreMerchantId store, CategoryReference reference)
            throws CategoryReferenceUnresolvableException {
        Optional<Category> category = reference.getId() != null
                ? categoryRepository.findByStoreAndId(store, reference.getId())
                : categoryRepository.findByStoreAndCode(store, reference.getCode());
        return category.orElseThrow(() -> CategoryReferenceUnresolvableException.of(
                reference.getId() != null ? reference.getId() : reference.getCode(), store));
    }
}
