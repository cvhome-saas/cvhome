package com.asrevo.cvhome.catalog.services.category;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.catalog.entity.Category;
import com.asrevo.cvhome.catalog.entity.Product;
import com.asrevo.cvhome.catalog.errors.CategoryFriendlyUrlNotFoundException;
import com.asrevo.cvhome.catalog.errors.CategoryNotFoundException;
import com.asrevo.cvhome.catalog.errors.CategoryReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.model.category.CategoryReference;
import com.asrevo.cvhome.catalog.model.category.PersistableCategory;
import com.asrevo.cvhome.catalog.model.category.ReadableCategory;
import com.asrevo.cvhome.catalog.repositories.CategoryRepository;
import com.asrevo.cvhome.catalog.repositories.ProductRepository;
import com.asrevo.cvhome.catalog.services.Pages;
import com.asrevo.cvhome.catalog.services.product.ProductService;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.model.entity.ReadableEntityList;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private static final long ROOT = -1L;

    private final CategoryRepository categoryRepository;

    private final ProductRepository productRepository;

    private final ProductService productService;

    @Override
    @Transactional(readOnly = true)
    public ReadableEntityList<ReadableCategory> list(StoreMerchantId store, String name, LanguageCode language,
                                                     boolean allLanguages, Pageable pageable) {
        Page<Category> page = pageOf(store, name, pageable);
        return Pages.toReadable(page, c -> CategoryMapper.toReadable(c, language, allLanguages));
    }

    @Override
    @Transactional(readOnly = true)
    public ReadableEntityList<ReadableCategory> hierarchy(StoreMerchantId store, String name, LanguageCode language,
                                                          boolean allLanguages, Pageable pageable) {
        Page<Category> page = pageOf(store, name, pageable);
        Map<Long, ReadableCategory> byId = new LinkedHashMap<>();
        page.getContent().forEach(c -> byId.put(c.getId(), CategoryMapper.toReadable(c, language, allLanguages)));
        List<ReadableCategory> roots = new ArrayList<>();
        for (ReadableCategory category : byId.values()) {
            ReadableCategory parent = category.getParent() == null ? null : byId.get(category.getParent().getId());
            if (parent == null) {
                roots.add(category); // a parent outside this page makes the node a root of what was read
            } else {
                parent.getChildren().add(category);
            }
        }
        return Pages.of(roots, page.getTotalElements(), page.getTotalPages(), page.getNumber());
    }

    @Override
    @Transactional(readOnly = true)
    public ReadableCategory get(StoreMerchantId store, Long id, LanguageCode language)
            throws CategoryNotFoundException {
        Category category = require(store, id);
        ReadableCategory readable = CategoryMapper.toReadable(category, language, true);
        readable.setChildren(categoryRepository.findSubtree(store, category.subtreePrefix()).stream()
                .filter(c -> !c.getId().equals(id))
                .map(c -> CategoryMapper.toReadable(c, language, true))
                .toList());
        return readable;
    }

    @Override
    @Transactional(readOnly = true)
    public ReadableCategory getByFriendlyUrl(StoreMerchantId store, String friendlyUrl, LanguageCode language)
            throws CategoryFriendlyUrlNotFoundException {
        Category category = categoryRepository.findByStoreAndFriendlyUrl(store, friendlyUrl, language)
                .orElseThrow(() -> CategoryFriendlyUrlNotFoundException.of(friendlyUrl, store));
        return CategoryMapper.toReadable(category, language, false);
    }

    @Override
    @Transactional(readOnly = true)
    public ReadableEntityList<ReadableCategory> listByProduct(StoreMerchantId store, Long productId,
                                                              LanguageCode language) {
        return Pages.single(categoryRepository.findByProduct(store, productId).stream()
                .map(c -> CategoryMapper.toReadable(c, language, true))
                .toList());
    }

    @Override
    public boolean exists(StoreMerchantId store, String code) {
        return categoryRepository.existsByStoreMerchantIdAndCode(store, code);
    }

    @Override
    @Transactional
    public PersistableCategory save(StoreMerchantId store, PersistableCategory source)
            throws CategoryNotFoundException, CategoryReferenceUnresolvableException {
        boolean creating = source.getId() == null || source.getId() <= 0;
        Category category = creating ? newCategory(store) : require(store, source.getId());
        CategoryMapper.apply(source, category);
        Category parent = resolveParent(store, source.getParent());
        // The descendants are found by the path they still hold, so it has to be read before the node moves.
        String previousPrefix = creating ? null : category.subtreePrefix();
        if (creating) {
            category = categoryRepository.saveAndFlush(category); // the lineage needs the generated id
        }
        category.placeUnder(parent);
        if (!creating) {
            replace(store, category, previousPrefix); // an edit may have re-parented: descendants follow
        }
        categoryRepository.save(category);
        source.setId(category.getId());
        return source;
    }

    @Override
    @Transactional
    public void setVisible(StoreMerchantId store, Long id, boolean visible) throws CategoryNotFoundException {
        require(store, id).setVisible(visible);
    }

    @Override
    @Transactional
    public void move(StoreMerchantId store, Long id, Long parentId) throws CategoryNotFoundException {
        Category category = require(store, id);
        String previousPrefix = category.subtreePrefix();
        Category parent = parentId == null || parentId == ROOT ? null : require(store, parentId);
        category.placeUnder(parent);
        replace(store, category, previousPrefix);
    }

    @Override
    @Transactional
    public void delete(StoreMerchantId store, Long id) throws CategoryNotFoundException {
        Category category = require(store, id);
        List<Category> subtree = categoryRepository.findSubtree(store, category.subtreePrefix());
        List<Long> ids = subtree.stream().map(Category::getId).toList();
        for (Product product : productRepository.findByStoreAndCategories(store, ids)) {
            product.getCategories().removeIf(c -> ids.contains(c.getId()));
            if (product.getCategories().isEmpty()) {
                productService.delete(store, product);
            }
        }
        // deepest first, so no child still points at a parent being removed
        categoryRepository.deleteAll(subtree.reversed());
    }

    /**
     * Rewrites the paths of every descendant after {@code moved} changed place.
     *
     * <p>
     * {@code previousPrefix} is the path the node held <em>before</em> the move, and it is not a convenience: the
     * descendants are still stored under it, so looking them up by the new one found nothing and left every child
     * of a moved category pointing at a path no ancestor has any more.
     * </p>
     */
    private void replace(StoreMerchantId store, Category moved, String previousPrefix) {
        Map<Long, Category> byId = new LinkedHashMap<>();
        byId.put(moved.getId(), moved);
        for (Category node : categoryRepository.findSubtree(store, previousPrefix)) {
            if (!node.getId().equals(moved.getId())) {
                byId.put(node.getId(), node);
            }
        }
        // findSubtree orders by lineage, so a parent is always re-placed before its children
        for (Category node : byId.values()) {
            if (!node.getId().equals(moved.getId())) {
                node.placeUnder(byId.getOrDefault(node.getParent().getId(), node.getParent()));
            }
        }
    }

    private Category resolveParent(StoreMerchantId store, CategoryReference reference)
            throws CategoryReferenceUnresolvableException {
        if (reference == null || reference.getId() == null && isBlank(reference.getCode())) {
            return null;
        }
        Optional<Category> parent = reference.getId() != null
                ? categoryRepository.findByStoreAndId(store, reference.getId())
                : categoryRepository.findByStoreAndCode(store, reference.getCode());
        return parent.orElseThrow(() -> CategoryReferenceUnresolvableException.of(
                reference.getId() != null ? reference.getId() : reference.getCode(), store));
    }

    private Category require(StoreMerchantId store, Long id) throws CategoryNotFoundException {
        return categoryRepository.findByStoreAndId(store, id).orElseThrow(() -> CategoryNotFoundException.of(id, store));
    }

    private static Category newCategory(StoreMerchantId store) {
        Category category = new Category();
        category.setStoreMerchantId(store);
        return category;
    }

    private Page<Category> pageOf(StoreMerchantId store, String name, Pageable pageable) {
        return isBlank(name) ? categoryRepository.findByStore(store, pageable)
                : categoryRepository.findByStoreAndName(store, name.trim(), pageable);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
